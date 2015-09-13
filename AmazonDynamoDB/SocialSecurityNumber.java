/******************************************************************************
* A SocialSecurityNumber object holds the value of a single social security
* number.  The methods of this class could be expanded to make the class more
* useful, but I have kept things basic to focus on how to implement the
* Comparable interface.
* <P>
* Note:
* A social security number is a nine-digit number broken into three groups
* in the form XXX-XX-XXXX.  The first three digits (the "area") are between
* 001 and 799 (inclusive).  The next two digits (the "group") are between
* 01 and 99 (inclusive).  The last four digits (the "serial") are between
* 0001 and 9999 (inclusive).
*
* @see
*   <A HREF="../../../../edu/colorado/simulations/SocialSecurityNumber.java">
*   Java Source Code for this class
*   (www.cs.colorado.edu/~main/edu/colorado/simulations/SocialSecurityNumber.java)
*   </A>
*
* @see
*   <A HREF="www.cpsr.org/cpsr/privacy/ssn/ssn.structure.html>
*   Structure of Social Security Numbers
*   (www.cpsr.org/cpsr/privacy/ssn/ssn.structure.html)
*   </A>
*
* @author Michael Main 
*   <A HREF="mailto:main@colorado.edu"> (main@colorado.edu) </A>
*
* @version
*   Jun 14, 2002
******************************************************************************/
public class SocialSecurityNumber implements Comparable
{
   final int MIN_SSN = 001010001; // Smallest legal ssn
   final int MAX_SSN = 799999999; // Biggest valid ssn
   final int SERIAL_LENGTH = 4;   // Number of digits in serial part
   final int GROUP_LENGTH = 2;    // Number of digits in group part
   final int WHOLE_LENGTH = 11;   // Number of characters in entire ssn
      
   private int number;      // Number given to this SSN (or zero if invalid)
   private boolean valid;   // True for a valid SSN; false for invalid number
     
   // These are all zero for an invalid ssn:
   private int area;    // First three digits: XXX-nn-nnnn
   private int group;   // Nest two digits: nnn-XX-nnnn
   private int serial;  // Last four digits: nnn-nn-XXXX

   // This is the word "InvalidSSN" for an invalid ssn:
   private StringBuffer whole;    // All of the SSN, in the form "nn-nnn-nnnn"

   /**
   * Construct a SocialSecurityNumber from a specified number.
   * @param n
   *   the social security number as an integer
   * @postcondition
   *   This SocialSecurityNumber has been created and initialized to n.
   **/
   public SocialSecurityNumber(int n)
   {
       set(n);
   }

   /**
   * Compare this SocialSecurityNumber to another.
   * @param o
   *   the other SocialSecurityNumber to compare to.
   * @return
   *   The return value is a positive number if
   *   my number is bigger than the other Object's number.
   *   The return value will be zero if my number is the same as
   *   the other's number. 
   *   The return value will be a negative number if my number is smaller than
   *   the other's number.
   * @exception ClassCastException
   *   Indicates that o is not a SocialSecurityNumber.
   **/
   public int compareTo(Object o)
   {
      return number - (((SocialSecurityNumber)o).number);
   }	

   /**
   * Set the value of this SocialSecurityNumber. 
   * @parma n
   *   the social security number as an integer
   * @postcondition
   *   This SocialSecurityNumber has been initialized to n.
   **/
   public void set(int n)
   {  
      area = n / 1000000;            // First three digits: XXX-nn-nnnn
      group = (n / 10000) % 100;     // Nest two digits: nnn-XX-nnnn
      serial = n % 10000;            // Last four digits: nnn-nn-XXXX
      
      valid =
	   ((n >= MIN_SSN) && (n <= MAX_SSN) && (group != 0) && (serial != 0));

      if (valid)
      {  // The ssn is valid, so create the whole string using the
	 // insert(0,...) method, which inserts the item at the start of the string.
         whole = new StringBuffer(WHOLE_LENGTH);
	 // Insert the last dash and four digits of the serial: "-XXXX":
	 whole.insert(0, serial);
	 while (whole.length( ) < SERIAL_LENGTH)
	      whole.insert(0, '0');
	 whole.insert(0, '-');
	 // Insert the other dash and the two digits of the group: "-XX-nnnn":
	 whole.insert(0, group);
	 while (whole.length( ) < GROUP_LENGTH + 1 + SERIAL_LENGTH)
	      whole.insert(0, '0');
	 // Insert the front three digits of the area: "XXX-nn-nnnn":
	 whole.insert(0, area);
	 while (whole.length( ) < WHOLE_LENGTH)
	      whole.insert(0, '0');
      }
      else
      {  // The ssn is invalid
	 whole = new StringBuffer("InvalidSSN");
         number = serial = group = area = 0;
      }
   }

   /**
   * Return the value of this Social Security Number as a String.
   * @return
   *   For a valid Social Security Number, the return value is a string of eleven
   *   characters with nine digits and two dashes in the format XX-XXX-XXXX.
   *   For an invalid Social Security Number, the return value is the string
   *   "InvalidSSN".
   **/
   public String toString( )
   {
      return whole.toString( );
   }
}