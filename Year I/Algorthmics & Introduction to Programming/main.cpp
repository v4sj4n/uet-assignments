/*
/////////////////////////////////////////////////////////////////////////////

Algorithmics & Introduction to Programming - Course Assignment

Write a C++ program for UET Student Contact Management System.
In this system, the user can create a new contact, search for a contact, edit and delete a contact and list all
the existing contacts.
Example: If user press 1, a new contact can be created. If user press 2, he/she will search for a contact, …
etc.

////////////////////////////////////////////////////////////////////////////
*/


#include <iostream>
#include <string>
#include <vector>
#include <regex>
#include <limits>
using namespace std;

// Class to represent a Contact with private properties and public methods
class Contact
{
private:
    // Private properties of a contact
    string fullName;     // Full name of the contact
    string phoneNumber;  // Phone number of the contact
    string eMail;        // Email address of the contact
    string dob;          // Date of birth of the contact
    string department;   // Department the contact belongs to

public:
    // Constructor to initialize a contact with all its details
    Contact(string fullName, string phoneNumber, string eMail, string dob, string department)
    {
        this->fullName = fullName;
        this->phoneNumber = phoneNumber;
        this->eMail = eMail;
        this->dob = dob;
        this->department = department;
    }

    // Getters to retrieve contact details
    string getName() const { return fullName; }
    string getPhone() const { return phoneNumber; }
    string getEmail() const { return eMail; }
    string getDOB() const { return dob; }
    string getDepartment() const { return department; }

    // Setters to update contact details
    void setName(string n) { fullName = n; }
    void setPhone(string p) { phoneNumber = p; }
    void setEmail(string e) { eMail = e; }
    void setDOB(string d) { dob = d; }
    void setDepartment(string d) { department = d; }
};

// Class to manage the contact system (add, search, edit, delete, list contacts)
class ContactManager
{
private:
    vector<Contact> contacts; // A list to store all contacts

    // Helper method to validate phone numbers using a specific format
    bool isValidPhone(const string &phone)
    {
        // Albanian phone number format: +3556[7-9]XXXXXXX
        regex pattern("^\\+3556[7-9][0-9]{7}$");
        return regex_match(phone, pattern);
    }

    // Helper method to validate email addresses (specific to UET domain)
    bool isValidEmail(const string &email)
    {
        // Email must have at least 3 characters before @uet.edu.al
        regex pattern("^[a-zA-Z0-9._%+-]{3,}@uet\\.edu\\.al$");
        return regex_match(email, pattern);
    }

    // Helper method to validate names (only letters and spaces allowed)
    bool isValidName(const string &name)
    {
        regex pattern("^[A-Za-z ]+$");
        return regex_match(name, pattern);
    }

    // Helper method to validate date of birth (format: DD-MM-YYYY)
    bool isValidDOBValue(const string &dob)
    {
        regex pattern("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(19|20)\\d{2}$");
        if (!regex_match(dob, pattern))
        {
            return false; // Invalid format
        }

        // Extract day, month, and year from the date string
        int day = stoi(dob.substr(0, 2));
        int month = stoi(dob.substr(3, 2));
        int year = stoi(dob.substr(6, 4));

        // Check for leap year if the month is February
        if (month == 2)
        {
            bool isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            if (day > (isLeapYear ? 29 : 28))
                return false;
        }
        // Check for months with 30 days
        else if (month == 4 || month == 6 || month == 9 || month == 11)
        {
            if (day > 30)
                return false;
        }

        return true; // Valid date
    }

    // Helper method to display a single contact's details
    void displayContact(const Contact &contact)
    {
        cout << "Name: " << contact.getName() << endl
             << "Phone: " << contact.getPhone() << endl
             << "Email: " << contact.getEmail() << endl
             << "Date of Birth: " << contact.getDOB() << endl
             << "Department: " << contact.getDepartment() << endl;
    }

public:
    // Method to clear the input stream to avoid input errors
    void clearInputStream()
    {
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
    }

    // Method to add a new contact to the system
    void addContact()
    {
        string name, phone, email, dob, department;

        cout << "\nEnter contact details:\n";

        // Input and validate name
        cout << "Name (letters only): ";
        clearInputStream();
        getline(cin, name);
        while (name.empty() || !isValidName(name))
        {
            cout << "Invalid name. Please use only letters and spaces: ";
            getline(cin, name);
        }

        // Input and validate phone number
        cout << "Phone (format: +3556[7-9]XXXXXXX): ";
        getline(cin, phone);
        while (!isValidPhone(phone))
        {
            cout << "Invalid phone number format. Please enter again: ";
            getline(cin, phone);
        }

        // Input and validate email
        cout << "Email (minimum 3 characters before @uet.edu.al): ";
        getline(cin, email);
        while (!isValidEmail(email))
        {
            cout << "Invalid email. Must be at least 3 characters followed by @uet.edu.al: ";
            getline(cin, email);
        }

        // Input and validate date of birth
        cout << "Date of Birth (format: DD-MM-YYYY): ";
        getline(cin, dob);
        while (!isValidDOBValue(dob))
        {
            cout << "Invalid date format or invalid date. Please enter again (DD-MM-YYYY): ";
            getline(cin, dob);
        }

        // Input department
        cout << "Department: ";
        getline(cin, department);
        while (department.empty())
        {
            cout << "Department cannot be empty. Please enter again: ";
            getline(cin, department);
        }

        // Add the new contact to the list
        contacts.emplace_back(name, phone, email, dob, department);
        cout << "\nContact added successfully!\n";
    }

    // Method to search for a contact by various criteria
    void searchContact()
    {
        if (contacts.empty())
        {
            cout << "\nNo contacts found in the system.\n";
            return;
        }

        cout << "\nSearch by:\n";
        cout << "1. Name\n";
        cout << "2. Phone\n";
        cout << "3. Email\n";
        cout << "4. Department\n";
        cout << "Enter your choice (1-4): ";

        int choice;
        clearInputStream();
        cin >> choice;
        clearInputStream();

        string query;
        cout << "Enter search query: ";
        getline(cin, query);

        bool found = false;
        for (const auto &contact : contacts)
        {
            // Check if the query matches the selected field
            if ((choice == 1 && contact.getName().find(query) != string::npos) ||
                (choice == 2 && contact.getPhone().find(query) != string::npos) ||
                (choice == 3 && contact.getEmail().find(query) != string::npos) ||
                (choice == 4 && contact.getDepartment().find(query) != string::npos))
            {
                displayContact(contact);
                cout << "----------------------------------------\n";
                found = true;
            }
        }

        if (!found)
        {
            cout << "No contacts found matching the query.\n";
        }
    }

    // Method to edit an existing contact
    void editContact()
    {
        if (contacts.empty())
        {
            cout << "\nNo contacts found in the system.\n";
            return;
        }

        cout << "\nCurrent contacts:\n";
        for (size_t i = 0; i < contacts.size(); ++i)
        {
            cout << "Position " << i << ": " << contacts[i].getName() << "\n";
        }

        int position;
        cout << "\nEnter position number to edit: ";
        clearInputStream();

        while (!(cin >> position) || position < 0 || position >= static_cast<int>(contacts.size()))
        {
            if (cin.fail())
            {
                cin.clear();
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
            }
            cout << "Invalid position. Please enter a number between 0 and "
                 << contacts.size() - 1 << ": ";
        }

        clearInputStream();

        auto &contact = contacts[position];
        cout << "\nCurrent contact details:\n";
        displayContact(contact);

        string name, phone, email, dob, department;

        cout << "\nEnter new details (press Enter to keep current value):\n";

        // Edit name
        cout << "Name (letters only) [" << contact.getName() << "]: ";
        getline(cin, name);
        if (!name.empty())
        {
            while (!isValidName(name))
            {
                cout << "Invalid name. Please use only letters and spaces: ";
                getline(cin, name);
            }
            contact.setName(name);
        }

        // Edit phone
        cout << "Phone (format: +3556[7-9]XXXXXXX) [" << contact.getPhone() << "]: ";
        getline(cin, phone);
        if (!phone.empty())
        {
            while (!isValidPhone(phone))
            {
                cout << "Invalid phone number format. Please enter again: ";
                getline(cin, phone);
            }
            contact.setPhone(phone);
        }

        // Edit email
        cout << "Email (minimum 3 characters before @uet.edu.al) [" << contact.getEmail() << "]: ";
        getline(cin, email);
        if (!email.empty())
        {
            while (!isValidEmail(email))
            {
                cout << "Invalid email. Must be at least 3 characters followed by @uet.edu.al: ";
                getline(cin, email);
            }
            contact.setEmail(email);
        }

        // Edit date of birth
        cout << "Date of Birth (format: DD-MM-YYYY) [" << contact.getDOB() << "]: ";
        getline(cin, dob);
        if (!dob.empty())
        {
            while (!isValidDOBValue(dob))
            {
                cout << "Invalid date format or invalid date. Please enter again (DD-MM-YYYY): ";
                getline(cin, dob);
            }
            contact.setDOB(dob);
        }

        // Edit department
        cout << "Department [" << contact.getDepartment() << "]: ";
        getline(cin, department);
        if (!department.empty())
        {
            contact.setDepartment(department);
        }

        cout << "\nContact updated successfully!\n";
    }

    // Method to delete a contact
    void deleteContact()
    {
        if (contacts.empty())
        {
            cout << "\nNo contacts found in the system.\n";
            return;
        }

        cout << "\nCurrent contacts:\n";
        for (size_t i = 0; i < contacts.size(); ++i)
        {
            cout << "Position " << i << ": " << contacts[i].getName() << "\n";
        }

        int position;
        cout << "\nEnter position number to delete: ";
        clearInputStream();

        while (!(cin >> position) || position < 0 || position >= static_cast<int>(contacts.size()))
        {
            if (cin.fail())
            {
                cin.clear();
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
            }
            cout << "Invalid position. Please enter a number between 0 and "
                 << contacts.size() - 1 << ": ";
        }

        // Remove the contact from the list
        contacts.erase(contacts.begin() + position);
        cout << "\nContact deleted successfully!\n";
    }

    // Method to list all contacts
    void listContacts()
    {
        if (contacts.empty())
        {
            cout << "\nNo contacts found in the system.\n";
            return;
        }

        cout << "\nAll Contacts:\n";
        cout << "----------------------------------------\n";
        for (const auto &contact : contacts)
        {
            displayContact(contact);
            cout << "----------------------------------------\n";
        }
    }
};

int main()
{
    ContactManager manager; // Create an instance of the ContactManager
    int choice;

    // Main menu loop
    while (true)
    {
        cout << "\nUET Student Contact Management System\n";
        cout << "1. Add New Contact\n";
        cout << "2. Search Contact\n";
        cout << "3. Edit Contact\n";
        cout << "4. Delete Contact\n";
        cout << "5. List All Contacts\n";
        cout << "6. Exit\n";
        cout << "Enter your choice (1-6): ";

        if (!(cin >> choice))
        {
            cout << "Invalid input. Please enter a number.\n";
            manager.clearInputStream();
            continue;
        }

        // Handle user choice
        switch (choice)
        {
        case 1:
            manager.addContact();
            break;
        case 2:
            manager.searchContact();
            break;
        case 3:
            manager.editContact();
            break;
        case 4:
            manager.deleteContact();
            break;
        case 5:
            manager.listContacts();
            break;
        case 6:
            cout << "\nThank you for using the system. Goodbye!\n";
            return 0;
        default:
            cout << "\nInvalid choice. Please try again.\n";
        }
    }
}
