

/**
 * @(#) Base64.java
 
 
 *
 * @author darkkazma@krbcp.com
 * @date   2017. 1. 18.
 * 
 * Create Date : 2005. 7. 12.
 * @author SaYeol Yang, eclips911@arirangsoln.com
 * @author YouBok Choi, lucky911@arirangsoln.com
 */
public class Base64 {
	
	/**
	 * byte[] ; encoding base64
	 * 
	 * @param raw byte[]
	 * 
	 * @return encoded String
	 */
	public static String encode(byte[] raw) {
		StringBuffer encoded = new StringBuffer();

		for (int i = 0; i < raw.length; i+=3) {
			encoded.append(encodeBlock(raw, i));
		}
		return encoded.toString();
	}

	/**
	 * Base64 encoding
	 * 
	 * @param raw encoding byte[]
	 * @param offset
	 * 
	 * @return encoding byte[]
	 */
	protected static char[] encodeBlock(byte[] raw, int offset) {
		int block = 0;
		int slack = raw.length - offset -1;
		int end = (slack >= 2) ? 2 : slack;

		for (int i = 0; i <= end ; i++) {
			byte b = raw[offset + i];
			int neuter = (b < 0) ? b + 256 : b;
			block += neuter << (8 * (2 - i));
		}

		char[] base64 = new char[4];
		for (int i = 0 ; i < 4; i++) {
			int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
			base64[i] = getChar(sixbit);
		}

		if (slack < 1) {
			base64[2] = '=';
		}
		if (slack < 2) {
			base64[3] = '=';
		}

		return base64;
	}

	/**
	 *
	 * 
	 * @param sixBit
	 * 
	 * @return character
	 */
	protected static char getChar(int sixBit) {
		if (sixBit >= 0 && sixBit <= 25)
			return (char)('A' + sixBit);
		if (sixBit >= 26 && sixBit <= 51)
			return (char)('a' + (sixBit-26));
		if (sixBit >= 52 && sixBit <= 61)
			return (char)('0' + (sixBit-52));
		if (sixBit == 62) return '+';
		if (sixBit == 63) return '/';

		return '?';
	}

	/**
	 * String; decoding
	 * 
	 * @param base64 decoding String
	 * 
	 * @return decoding  byte[]
	 */
	public static byte[] decode(String base64) {
		int pad = 0, i = 0;

        for (i = base64.length() - 1; base64.charAt(i) == '=' ; i--)
            pad++;


		int length = base64.length() * 6 / 8 - pad;
		byte[] raw = new byte[length];

		int rawIndex = 0;
		int block = 0, count = base64.length();
		for (i = 0 ; i < count ; i += 4) {
            int countOther = count % 4;
            if(countOther == 0){
                block = (getValue(base64.charAt(i)) << 18)
                        + (getValue(base64.charAt(i + 1)) << 12)
                        + (getValue(base64.charAt(i + 2)) << 6)
                        + (getValue(base64.charAt(i + 3)));
            }
            else if(countOther == 3){
                block = (getValue(base64.charAt(i)) << 18)
                        + (getValue(base64.charAt(i + 1)) << 12)
                        + (getValue(base64.charAt(i + 2)) << 6);
            }
            else if(countOther == 2){
                block = (getValue(base64.charAt(i)) << 18)
                        + (getValue(base64.charAt(i + 1)) << 12);
            }
            else if(countOther == 1){
                block = (getValue(base64.charAt(i)) << 18);
            }

			for (int j = 0; j < 3 && rawIndex + j < raw.length ; j++) {
				raw[rawIndex + j] = (byte)((block >> (8 * (2 - j))) & 0xff);
			}
			rawIndex += 3;
		}
		return raw;
	}

	/**
	 * character int
	 * 
	 * @param c character
	 * 
	 * @return character int 
	 */
	protected static int getValue(char c) {
		if (c >= 'A' && c <= 'Z')
			return c-'A';
		if (c >= 'a' && c <= 'z')
			return c-'a' + 26;
		if (c >= '0' && c <= '9')
			return c-'0' + 52;
		if (c == '+') return 62;
		if (c == '/') return 63;
		if (c >= '=') return 0;
		return -1;
	}

    public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage : java twins.Common.Util.Base64 [1/2] Passwd");
			System.out.println("                      1 : encode ");
			System.out.println("                      2 : decode ");
			System.exit(1);
		}

		int type = 0;
		String pwd = null;
        try {
			type = Integer.parseInt(args[0]);
			if (type == 1) {
				pwd = encode(args[1].getBytes());
				System.out.println("Passwd eecode : '" + args[1] + "' -> '" +
								   pwd + "'");
			}
			else if (type == 2) {
				pwd = new String(decode(args[1]));
				System.out.println("Passwd decode : '" + args[1] + "' <- '" +
								   pwd + "'");
			}
			else {
				System.out.println("Usage : java twins.Common.Util.Base64 [1/2] Passwd");
				System.out.println("                      1 : encode ");
				System.out.println("                      2 : decode ");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}