package banking;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        Connect conn = new Connect(args[1]);
        conn.connect();

        Scanner scanner = new Scanner(System.in);
        int choice = 3;
        while (choice != 0) {
            displayMainMenu();
            choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 1) {
                String newCardNumber = generateCardNumber();
                String newPinNumber = generatePinNumber();
                while (!conn.createNewAccount(newCardNumber, newPinNumber)) {
                    newCardNumber = generateCardNumber();
                    newPinNumber = generatePinNumber();
                }
                displayAccountCompletion(newCardNumber, newPinNumber);
            } else if (choice == 2) {
                String cardNumber = validateCardNumber();
                String pinNumber = conn.findAccountNumber(cardNumber);
                if (pinNumber.equals("Error")) {
                    System.out.println("Invalid card number");
                } else {
                    String enteredPinNumber = validatePinNumber();
                    if (enteredPinNumber.equals(pinNumber)) {
                        System.out.println("You have successfully logged in!\n");
                        int account_choice = displayAccountMenu();
                        while (account_choice != 0) {
                            int accountBalance = conn.findAccountBalance(cardNumber);
                            if (account_choice == 1) {
                                System.out.println("Balance: " + accountBalance);
                                account_choice = displayAccountMenu();
                            } else if (account_choice == 2) {
                                System.out.println("Enter Income:");
                                String transactedAmount = scanner.nextLine();

                                if (conn.addIncome(cardNumber, transactedAmount)) {
                                    System.out.println("Income was added!");
                                    account_choice = displayAccountMenu();
                                }

                            } else if (account_choice == 3) {
                                System.out.println("Transfer\n"
                                        + "Enter card number:");
                                String receiverCardNumber = scanner.nextLine();
                                if (receiverCardNumber.equals(cardNumber))
                                    System.out.println("You can't transfer money to the same account!");
                                else {
                                    if (checkValidCard(receiverCardNumber) && conn.checkAccountExists(receiverCardNumber)) {
                                        System.out.println("Enter how much money you want to transfer:");
                                        String transactedAmount = scanner.nextLine();
                                        if (Integer.parseInt(transactedAmount) > accountBalance) {
                                            System.out.println("Not enough money!");
                                        } else if (conn.performTransfer(cardNumber, receiverCardNumber, transactedAmount)) {
                                            System.out.println("Success!");
                                        }
                                    } else {
                                        System.out.println("Account does not exist");
                                    }
                                }
                                account_choice = displayAccountMenu();
                            } else if (account_choice == 4) {
                                if (conn.deleteAccount(cardNumber)) {
                                    System.out.println("The account has been closed!");
                                    break;
                                }
                            } else if (account_choice == 5) {
                                break;
                            } else {
                                System.out.println("Invalid Choice");
                            }
                        }
                        if (account_choice == 0) {
                            choice = 0;
                        }
                    } else {
                        System.out.println("Invalid PIN number");
                    }
                }
            }
        }
    }

    // ------------ Displays -------------//
    public static void displayMainMenu() {
        System.out.println(
                "1. Create an account "
                        + "\n2. Log into account "
                        + "\n0. Exit "
        );
    }

    public static void displayAccountCompletion(String newCardNumber, String pinNumber) {
        System.out.println(
                "Your card has been created"
                        + "\nYour card number:\n"
                        + newCardNumber
                        + "\nYour card PIN:\n"
                        + pinNumber
                        + '\n'
        );
    }

    public static int displayAccountMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(
                "1. Balance\n"
                        + "2. Add income\n"
                        + "3. Do transfer\n"
                        + "4. Close account\n"
                        + "5. Log out\n"
                        + "0. Exit"
        );
        int choice = scanner.nextInt();
        scanner.nextLine();
        return choice;
    }

    // ------------ Generation -------------//

    public static String generateCardNumber() {

        // Luhn's algorithm
        long number = ThreadLocalRandom.current().nextLong(0, 1_000_000_000L);

        String number_string = String.format("%09d", number);
        int result = 8;
        for (int i = 0; i < number_string.length(); i++) {
            int tmp = Integer.parseInt(String.valueOf(number_string.charAt(i)));
            result += (i % 2 == 0)
                    ? (tmp * 2 > 9) ? (tmp * 2) - 9 : tmp * 2
                    : tmp;
        }
        int result_modulo_10 = result % 10;
        result = (result_modulo_10 == 0) ? 0 : 10 - result_modulo_10;
        return "400000" + number_string + result;
    }

    public static String generatePinNumber() {
        int number = (int) (Math.random() * 10000);
        return String.format("%04d", number);
    }

    // ------------ Validation -------------//

    public static String validateCardNumber() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your card number:");
        return scanner.nextLine();
    }

    public static String validatePinNumber() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your PIN:");
        return scanner.nextLine();
    }

    public static boolean checkValidCard(String newCardNumber) {
        int cardLength = newCardNumber.length();
        if (cardLength == 16) {
            int result = 0;
            for (int i = 0; i < 15; i++) {
                int tmp = Integer.parseInt(String.valueOf(newCardNumber.charAt(i)));
                result += (i % 2 == 0)
                        ? (tmp * 2 > 9) ? (tmp * 2) - 9 : tmp * 2
                        : tmp;
            }
            int result_modulo_10 = result % 10;
            int lastDigit = (result_modulo_10 == 0) ? 0 : 10 - result_modulo_10;
            if (lastDigit != Integer.parseInt(String.valueOf(newCardNumber.charAt(15)))) {
                System.out.println("Probably you made a mistake in the card number. Please try again!");
                return false;
            }
            return true;
        } else {
            System.out.println("Invalid Card Number");
            return false;
        }
    }
}
