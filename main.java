import java.util.Scanner;

public class App {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

        /*
            reads plaintext and a columnar transposition key, validates
            input, computes table dimensions, then encrypts and decrypts to demonstrate the
            Tabular Transposition Cipher. Repeats until the user quits.
        */
		try {
			boolean runAgain = true;
			while (runAgain) {
				try {
					System.out.println("=== Tabular TC ===");
					System.out.print("Plain Text: ");
					String pt = scanner.nextLine();
					if (pt == null) {
						pt = "";
					}

					// Disallow numbers in plaintext, reprompt until valid
					while (containsDigit(pt)) {
						System.out.println("Error: Plaintext must not contain numbers (0-9).");
						System.out.print("Plain Text: ");
						pt = scanner.nextLine();
						if (pt == null) {
							pt = "";
						}
					}

					System.out.print("Auto Key: ");
					if (!scanner.hasNextInt()) {
						System.out.println("Error: Key must be a positive integer composed of digits 1..9.");
						scanner.nextLine(); // consume invalid input line
						continue;
					}
					int key = scanner.nextInt();
					if (key <= 0) {
						System.out.println("Error: Key must be greater than 0.");
						scanner.nextLine(); // consume newline after int
						continue;
					}
					String keyString = Integer.toString(key);
					// consume the remaining newline after nextInt
					scanner.nextLine();
					if (keyString.isEmpty()) {
						System.out.println("Error: Key must not be empty.");
						continue;
					}
					// Validate that key digits are within 1..n and form a permutation
					int numCols = keyString.length();
					if (!isValidKeyDigits(keyString)) {
						System.out.println("Error: Key must contain digits 1..9 only, no zeros or letters.");
						continue;
					}
					if (!isPermutationOfRange(keyString, numCols)) {
						System.out.println("Error: Key must be a permutation of 1.." + numCols + ". Example for 3: 123, 132, 213, 231, 312, 321.");
						continue;
					}
					System.out.println("Number of columns: " +numCols);

					int offsetCharLength = (pt.length() % numCols) == 0 ? 0 : keyString.length() - (pt.length() % numCols);
					if (offsetCharLength < 0) {
						System.out.println("Error: Computed offset was negative, aborting.");
						continue;
					}
					int numRows = (pt.length() + offsetCharLength) / numCols ;
					System.out.println("Number of rows: " +numRows);
					
					String encryptedText = encrypt(pt, keyString, numCols, numRows, offsetCharLength);
					System.out.println("Encrypted Text: " +encryptedText +"\n");

					String decryptedText = decrypt(encryptedText, keyString, numCols, numRows, offsetCharLength);
					if (offsetCharLength > decryptedText.length()) {
						System.out.println("Error: Offset longer than decrypted text. Check key and input.");
						continue;
					}
					System.out.println("Decrypted Text: " +decryptedText);
				} catch (IndexOutOfBoundsException ex) {
					System.out.println("Runtime error: Index out of bounds. Please verify key digits are within 1..n and unique.");
				} catch (IllegalArgumentException ex) {
					System.out.println("Invalid input: " + ex.getMessage());
				} catch (Exception ex) {
					System.out.println("Unexpected error: " + ex.getMessage());
				}

				System.out.print("Do you want to encrypt another text? (y/n): ");
				String answer = scanner.nextLine().trim().toLowerCase();
				runAgain = answer.startsWith("y");
			}
		} finally {
			scanner.close();
		}
	}

	/**
	 * Validates that every character in the key string is a digit between 1 and 9.
	 *
	 * @param keyString digits-only key text
	 * @return true if all chars are in '1'..'9'; false otherwise
	 */
    private static boolean isValidKeyDigits(String keyString) {
        for (int i = 0; i < keyString.length(); i++) {
            char ch = keyString.charAt(i);
            if (ch < '1' || ch > '9') {
                return false;
            }
        }
        return true;
    }

	/**
	 * Ensures the key is a permutation of 1..numCols (each digit appears exactly once
	 * and every digit is within range).
	 *
	 * @param keyString digits-only key text
	 * @param numCols   expected size of the permutation
	 * @return true if keyString forms a valid permutation of 1..numCols
	 */
    private static boolean isPermutationOfRange(String keyString, int numCols) {
        boolean[] seen = new boolean[numCols];
        for (int i = 0; i < keyString.length(); i++) {
            int digit = keyString.charAt(i) - '0';
            if (digit < 1 || digit > numCols) {
                return false;
            }
            if (seen[digit - 1]) {
                return false;
            }
            seen[digit - 1] = true;
        }
        // verify all 1..numCols seen
        for (int i = 0; i < numCols; i++) {
            if (!seen[i]) return false;
        }
        return true;
    }

	/**
	 * Returns true if the provided text contains any numeric digit.
	 * Allows letters, spaces, and symbols, but rejects 0-9 anywhere in the text.
	 */
	private static boolean containsDigit(String text) {
		if (text == null) return false;
		for (int i = 0; i < text.length(); i++) {
			if (Character.isDigit(text.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/*
	    Encrypts using Columnar/Tabular Transposition.
        Pads plaintext with 'z' to fill the final row.
        Fills a numRows x numCols table row-wise.
        Reads columns in the order dictated by key digits (1-based) to produce ciphertext.
        Also provides/prints the filled table for visualization.
	*/ 
    public static String encrypt(String text, String keyString, int numCols, int numRows, int offsetCharLength) {
		// 2d array that will act as the table
		char td[][] = new char[numRows][numCols];
		
		// Essential for tracking the current character to be assigned in that specific row and column of the table (just like a cursor).
		int ptIndex = 0;
		String encryptedText = ""; // The Encrypted Text pretty self explanatory
		
		// Adding the extra characters needed at the end of the plain text to fill the table. 
		for (int i = 0; i < offsetCharLength; i++) {
			text += "z"; // Concatenate extra characters to the text variable
		}

        // Looping through the table's column first and then the row and assign that single character from the plain text based from the ptIndex(cursor).
        /* Ex.  —————-
                    | 1 | 2 | 3 |
                    —————-
                    | 4 | 5 | 6 |
                    —————-
                    */
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				td[row][col] = text.charAt(ptIndex); // Gets a single character from the plain text based from the ptIndex and store it at a specific row and column of the 2d array
				
				// Incrementing the so called cursor, so it will not only repeat the first character from the plain text, and will actually iterate over the plain text. 
				ptIndex++;
			}
		}

        // Called the method displayTable with the necessary data as the parameter to display the table. 
		displayTable(td, numCols, numRows);

        // "Outer Loop" will loop though each of the key (gets the length of the key as its limit for looping (basically the number of columns)).
	    for (int currentKeyIndex = 0; currentKeyIndex < keyString.length(); currentKeyIndex++) {
	        /*
	            Gets a single character from the key as a string using the outerloop index and convert it to an integer. ex. key is 312,
	            on the first loop this operation will get the ''3" as an integer, and so on. 
            */
			int col = Character.getNumericValue(keyString.charAt(currentKeyIndex));
			
			/*
			    Iterates through the row of a column based (previous operation)
			    ex. the col = 3, this will iterate all the rows on the column 3, and add (concatenate) the character on that specific column and row to the encryptedText variable
                ex. the character on column 3 row 1 is "Q" and the character on column 3 row 2 is "W" the variable encryptedText will hold "QW"
			*/
			for (int row = 0; row < numRows; row++) {
				encryptedText += td[row][col - 1];
			}
		}

        // Return the encrypted text to the caller
		return encryptedText;
	}
	
	/*
        Decrypts a Columnar/Tabular Transposition ciphertext.
	    Splits ciphertext into numCols groups of length numRows (visual guide printed).
	    Places each group into the column indicated by the key digit (1-based) top-to-bottom.
	    Reads table row-wise to reconstruct plaintext, then removes padding.
        Also prints intermediate boxes and the reconstructed table.
	 */
	public static String decrypt(String text, String keyString, int numCols, int numRows, int offsetCharLength) {
		// Declare a variable that acts as a cursor (just like from the encryption method)
		int ptIndex = 0;
		
		// 2d array as a table
		char td[][] = new char[numRows][numCols];
		
		// This is used for grouping a set number of characters before putting it in the table (2d array)
		String textGroup[] = new String[numCols];
		String decryptedText = ""; // The Decrypted Text pretty self explanatory
		
		// This method displays where and what column to insert that specific group of characters
		printBoxIndicator(numCols, numRows, keyString, false); System.out.println();
		
		printHLineBox(numCols, numRows); // This method prints the top outline of the grouped text
		
		// We used nested ''For Loop" for grouping the characters based on the numbers of rows, and displaying them
		for (int col = 0; col < numCols; col++) {
			String groupedText = ""; // Note that this variable resets every iteration of the column, so we get new grouped text every column
			for (int row = 0; row < numRows; row++) {
				groupedText += text.charAt(ptIndex); // Gets a single character from the encrypted text based from the ptIndex and add (concatenate) it to the groupedText variable
				
				ptIndex++;
			}
			System.out.print("| " +groupedText +" |  "); // Prints the grouped text with barriers ( | ) separating each group
			textGroup[col] = groupedText; // Stored the grouped text based on the column it belonged for later use
		}
		System.out.println();
		printHLineBox(numCols, numRows); // This method prints the bottom outline of the grouped text
		
		// This method displays where and what column to insert that specific group of characters
		printBoxIndicator(numCols, numRows, keyString, true); System.out.println();
		
		// "Outer Loop" will loop though each of the key (gets the length of the key as its limit for looping (basically the number of columns))
	    for (int currentKeyIndex = 0; currentKeyIndex < keyString.length(); currentKeyIndex++) {
	        // Gets a single character from the key as a string using the outerloop index and convert it to an integer
			int col = Character.getNumericValue(keyString.charAt(currentKeyIndex));
			
		    /*
		        Retrieve a grouped characters based from the outerloop index,
		        and iterate throught the grouped characters to get a single character,
		        and assign it in its corresponding row and column.
		        Noticed the col inside the loop is subtracted by 1, because an index and a length is not the same in an array, so we had to account for that difference 
		    */
			for (int row = 0; row < numRows; row++) {
				td[row][col-1] = textGroup[currentKeyIndex].charAt(row);
			}
		}
		
		// Displays a table based from the data (args) passed
		displayTable(td, numCols, numRows);
		
		// Finalize the encrypted text, iterate through the column first and then the row
        /* Ex.  —————-
                    | 1 | 2 | 3 |
                    —————-
                    | 4 | 5 | 6 |
                    —————-
                    */
		for(int row = 0; row < numRows; row++){
		    for(int col = 0; col < numCols; col++){
		        decryptedText += td[row][col];
		    }
		}
		
		// We used the String Method "substring" to slice off the extra characters. syntax is String.substring(start index, end index);
		// So we used 0 as the starting index, and we calculate the end index by getting the length of the decrypted text and subtracting it to the offsetCharLength (previous algorithm where we calculate the number of extra characters (Line 24))
		decryptedText = decryptedText.substring(0, decryptedText.length() - offsetCharLength);
		
		// Return the decrypted text to the caller
		return decryptedText;
	}
	
    //Prints a table visualization with row and column indicators for the provided 2D character array.
	public static void displayTable(char td[][], int numCols, int numRows){
	    // Displays the number of columns
	    columnIndicator(numCols);

		for (int row = 0; row < numRows; row++) {
			boolean isFirst = true; // Resets every row, so we can print the row indicator
			printHLineTable(numCols); // Displays a horizontal line of the table (top)
			
			// Loop until the number of columns is reached
			for (int col = 0; col < numCols; col++) {
				/*
				    We used an IF statement to determine the first loop for this specific column, 
				    if it is indeed the first loop for this column we will display the row indicator and the value for that specific row and column 
				    else we will just display the value for that specific row and column 
                */
				if (isFirst) {
					System.out.print("| " +(row + 1) +" | " +td[row][col] +" "); // Includes the necessary barriers ( | ) and spacing for each character
				} else {
					System.out.print("| " +td[row][col] +" "); // Same as the above but without the row indicator
				}

				isFirst = false; // Make the value "false" because we dont want to display another row indicator when the loop moves to another column. 
			}
			System.out.println("|"); // Account for the last closing barrier ( | )
		}
		printHLineTable(numCols); // Displays a horizontal line of the table (bottom)
	}

    // Prints the header row that labels columns from 0..numCols.
	public static void columnIndicator(int numCols) {
		// Prints a horizontal line based from the number of columns
		printHLineTable(numCols);
		
		// Will loop until the number of columns is reached printing the current column and  the necessary barrier ( | )
		for (int i = 0; i <= numCols; i++) {
			System.out.print("| " +i +" ");
		}
		System.out.println("|"); // Account for the last closing barrier ( | )
	}

    // Prints a horizontal separator line sized for a table with the given columns.
	public static void printHLineTable(int numCols) {
		System.out.print("-"); // Account for the first barrier ( | )
		
		// Loop until the number of columns is reached printing the necessary horizontal line
		for (int i = 0; i <= numCols; i++) {
			System.out.print("———-");
		}
		System.out.println(); // Moves to the next line
	}
	
	// Prints a horizontal separator for the boxed ciphertext groups view used during decryption, sized by columns and rows.
	public static void printHLineBox(int numCols, int numRows) {
		// Prints a horizontal line for the grouped text
		for(int i = 0; i < numCols; i++){
    		System.out.print("——"); // Accounts for the barrier ( | ) and 1 space before each group
    		for (int j = 0; j < numRows; j++) {
    			System.out.print("—"); // Print this for every characters in that group
    		}
    		System.out.print("——  "); // Accounts for the barrier ( | ) and 2 spaces after each group
		}
		System.out.println(); // Moves to the next line
	}
	
	/*
	    Prints indicators above the boxed ciphertext groups. When isOffset is false,
	    prints simple 1..numCols; when true, prints the actual key digits over each
	    group to show mapping during decryption.
    */
	
	public static void printBoxIndicator(int numCols, int numRows, String keyString, boolean willSubstitute){
	    // Displays the necessary indicators for the grouped text
	    for(int i = 0; i < numCols; i++){
		    System.out.print("  "); // Accounts for the barrier ( | ) and 1 space before each group
		    for(int j = 0; j < numRows/2; j++){
		        System.out.print(" "); // Add the necessary spacing, we divide the number of rows by 2 because we want the indicators to be at the middle of the group (box)
		    }
		    
		    // Will what we want to display ex. the number of groups or the keys we used for encryption
		    if(willSubstitute){
		        System.out.print(keyString.charAt(i)); // Displays the key we used for encryption at the top of a specific group (box)
		    }else{
		        System.out.print(i+1); // Displays the number for columns. Notice we add 1 to the iterator, because an index is not the same as a length. In this case the first loop will display 0 but the grouped text (box) doesn't start at 0, so we had to account for that and add 1.
		    }
		    
		    for(int j = 0; j < numRows/2; j++){
		        System.out.print(" "); // Add the necessary spacing, we divide the number of rows by 2 because we want the indicators to be at the middle of the group (box)
		    }
		    
		    // This is used for centering, because if the number of characters in a grouped text is even there is no center, so we had to account for that. 
		    if(numRows%2 == 0){ // Modulus operator (%) returns the remainder from a division operation. We used this for determining whether a number is even or odd
		        System.out.print("   "); // If the number of characters in a grouped text is even we add 3 spaces
		    }else{
		        System.out.print("    "); // else if it is odd we add 4 spaces. see example below. 
		    } /* P.S. substitute "–" as a space from line 232 and 243, and "+" as space from line 248 and 250. 
		          –––1–––+++–––2–––          See the indicator (1) is somewhat at the
		        | ABCDEF |      | GHIJKL |            center if we add 3 spaces when the length
		                                                              of the grouped text is even. (recap: text length is 6, 6/2 = 3("–" spaces from line 232 243) remainder 0)(=0 passed the if condition)
		                                                              
		          ––1––++++––2––
		        | ABCDE |      | FGHIJ |                and now the indicator is at the center
                                                                     if we add 4 spaces when the length of the
                                                                     grouped text is odd. (recap: text length is 5, 5/2 = 2("–" spaces from line 232 243) remainder 1)(≠0 passed the else)
		    */
		}
	}
}