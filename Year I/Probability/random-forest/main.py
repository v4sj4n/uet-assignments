import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split, RandomizedSearchCV
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.feature_selection import SelectFromModel

# Load and enhance data
df = pd.read_csv("kc_house_data.csv")

# Advanced feature engineering
df["house_age"] = 2015 - df["yr_built"]
df["is_renovated"] = (df["yr_renovated"] > 0).astype(int)

# Zipcode features
zipcode_stats = df.groupby('zipcode')['price'].agg(['mean', 'median', 'count']).reset_index()
zipcode_stats.columns = ['zipcode', 'zipcode_price_mean', 'zipcode_price_median', 'zipcode_count']
df = df.merge(zipcode_stats, on='zipcode')

# Location clustering
from sklearn.cluster import KMeans
kmeans = KMeans(n_clusters=12, random_state=42)
df['location_cluster'] = kmeans.fit_predict(df[['lat', 'long']])

# Cluster stats
cluster_stats = df.groupby('location_cluster')['price'].agg(['mean', 'std']).reset_index()
cluster_stats.columns = ['location_cluster', 'cluster_price_mean', 'cluster_price_std']
df = df.merge(cluster_stats, on='location_cluster')

# Random Forest friendly features
df["living_to_lot_ratio"] = df["sqft_living"] / (df["sqft_lot"] + 1)
df["basement_ratio"] = df["sqft_basement"] / (df["sqft_living"] + 1)
df["bath_per_bed"] = df["bathrooms"] / (df["bedrooms"] + 1)
df["grade_view_interaction"] = df["grade"] * (df["view"] + 1)
df["age_grade"] = df["house_age"] * df["grade"]
df["condition_grade"] = df["condition"] * df["grade"]

# Boolean features
df["has_basement"] = (df["sqft_basement"] > 0).astype(int)
df["has_view"] = (df["view"] > 0).astype(int)
df["high_grade"] = (df["grade"] >= 8).astype(int)
df["waterfront_view"] = df["waterfront"] * (df["view"] + 1)

# Remove outliers
df = df[(df["price"] >= 75000) & (df["price"] <= 1500000)]
df.dropna(inplace=True)

# Feature selection
features = [
    "bedrooms", "bathrooms", "sqft_living", "sqft_lot", "floors",
    "waterfront", "view", "condition", "grade", "sqft_above", "sqft_basement",
    "lat", "long", "house_age", "is_renovated", "zipcode_price_mean",
    "living_to_lot_ratio", "basement_ratio", "bath_per_bed", 
    "grade_view_interaction", "cluster_price_mean", "age_grade",
    "condition_grade", "has_basement", "has_view", "high_grade", "waterfront_view"
]

X = df[features]
y = np.log1p(df["price"])

# Train-test split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Hyperparameter tuning
param_dist = {
    'n_estimators': [500, 700, 1000],
    'max_depth': [25, 30, None],
    'min_samples_split': [2, 5, 8],
    'min_samples_leaf': [1, 2],
    'max_features': ['sqrt', 0.3, 0.5],
    'max_samples': [0.8, 0.9, None],
    'bootstrap': [True],
    'oob_score': [True]
}

rf_random = RandomizedSearchCV(
    RandomForestRegressor(random_state=42, n_jobs=-1),
    param_distributions=param_dist,
    n_iter=30,
    cv=5,
    scoring='neg_mean_squared_error',
    n_jobs=-1,
    random_state=42
)

rf_random.fit(X_train, y_train)
best_rf = rf_random.best_estimator_

# Feature selection with best model
selector = SelectFromModel(best_rf, threshold='0.75*median')
X_train_selected = selector.fit_transform(X_train, y_train)
X_test_selected = selector.transform(X_test)

# Final model with selected features
final_rf = RandomForestRegressor(**rf_random.best_params_)
final_rf.fit(X_train_selected, y_train)

# Predictions
y_pred_log = final_rf.predict(X_test_selected)
y_pred = np.expm1(y_pred_log)
y_test_actual = np.expm1(y_test)

rmse = np.sqrt(mean_squared_error(y_test_actual, y_pred))
r2 = r2_score(y_test_actual, y_pred)

print(f"Optimized RF RMSE: ${rmse:,.2f}")
print(f"Optimized RF R²: {r2:.4f}")
print(f"OOB Score: {final_rf.oob_score_:.4f}")

# Feature importance
importances = final_rf.feature_importances_
feature_names = X.columns[selector.get_support()]
for i, importance in enumerate(importances):
    print(f"{feature_names[i]}: {importance:.4f}")