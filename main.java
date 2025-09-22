import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("=== Tabular TNC niccceee g ===");
		System.out.print("Plain Text: ");
		String pt = scanner.nextLine();

		System.out.print("Auto Key: ");
		int key = scanner.nextInt();
		String keyString = Integer.toString(key);

		int numCols = keyString.length();
		System.out.println("Number of columns: " +numCols);

        /*
            We used ternary operator (short hand of the IF ELSE statement) in getting the number of extra characters to be displayed at the table.
            First we get the modulo of the length of the plain text and the number of columns, 
            if the modulo is 0 then we do no calculation and assign 0 to the offsetCharLength variable, 
            else we subtract the number of columns by the modulo of the length of the plain text and the number of columns. 
        */
		int offsetCharLength = (pt.length() % numCols) == 0 ? 0 : numCols - (pt.length() % numCols);
		
		// We then add the length of the plain text by the number of extra characters (offsetCharLength) and divide by the number of columns to get the number of rows. 
		int numRows = (pt.length() + offsetCharLength) / numCols ;
		System.out.println("Number of rows: " +numRows);
		
		// Calls the encrypt method passing the necessary variables (data) to do the encryption part. 
		String encryptedText = encrypt(pt, keyString, numCols, numRows, offsetCharLength);
		System.out.println("Encrypted Text: " +encryptedText +"\n");

        // Calls the decrypt method passing the necessary variables (data) to do the decryption part. 
		String decryptedText = decrypt(encryptedText, keyString, numCols, numRows, offsetCharLength);
		System.out.println("Decrypted Text: " +decryptedText);
	}

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

	public static void columnIndicator(int numCols) {
		// Prints a horizontal line based from the number of columns
		printHLineTable(numCols);
		
		// Will loop until the number of columns is reached printing the current column and  the necessary barrier ( | )
		for (int i = 0; i <= numCols; i++) {
			System.out.print("| " +i +" ");
		}
		System.out.println("|"); // Account for the last closing barrier ( | )
	}

	public static void printHLineTable(int numCols) {
		System.out.print("-"); // Account for the first barrier ( | )
		
		// Loop until the number of columns is reached printing the necessary horizontal line
		for (int i = 0; i <= numCols; i++) {
			System.out.print("———-");
		}
		System.out.println(); // Moves to the next line
	}
	
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
