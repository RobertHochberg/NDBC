package ndbc;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

public class DTeamPanel extends JPanel {
	

	public DTeamPanel() {
		super();
		this.setBackground(new Color(0, 35, 102));
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel keyPanel = new JPanel();
//		encryptPanel.setLayout(new BoxLayout(encryptPanel, BoxLayout.Y_AXIS));
		
		JTextField pField = new JTextField(20);
		JTextField gField = new JTextField(20);
		JButton generateGP = new JButton("Generate p and g");
		generateGP.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Random rnd = new Random();
				
				BigInteger q;
				BigInteger k = new BigInteger("2");
				BigInteger rv;
				do{
				q = new BigInteger(64, rnd);
				q = q.nextProbablePrime();
				
				
					System.out.println("Finding prime");
					rv = k.multiply(q).add(BigInteger.ONE);
					
				}while(!rv.isProbablePrime(7));
				
				pField.setText(rv.toString());
				
				BigInteger g = BigInteger.ONE;
				do{
					System.out.println("Finding Generator");
					g = g.add(BigInteger.ONE);
				}while((g.intValue() == 1) || (g.pow(k.intValue()).intValue() == 1) || (g.modPow(q, rv).intValue() == 1) );
				
				gField.setText(g.toString());
			}
		});
		keyPanel.add(generateGP);
		keyPanel.add(pField);
		keyPanel.add(gField);
		
		JLabel privateLabel = new JLabel();
		JButton generatePrivate = new JButton("Generate Secret Key");
		generatePrivate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to replace your secret key?", 
						"Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					BigInteger p = new BigInteger(pField.getText());
					BigInteger rv;
					do{
						Random rnd = new Random();
						rv = new BigInteger(p.bitLength(), rnd);
					}while(p.compareTo(rv) < 0);
					
					privateLabel.setText(rv.toString());
				}
				
			}
		});
		keyPanel.add(generatePrivate);		
		keyPanel.add(privateLabel);
		
		JTextField gPower = new JTextField();
		gPower.setEditable(false);
		JButton raiseGPower = new JButton("Raise Mod");
		raiseGPower.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				gPower.setText(new BigInteger(gField.getText())
						.modPow(new BigInteger(privateLabel.getText()), new BigInteger(pField.getText())).toString());
				
			}
		});
		keyPanel.add(raiseGPower);
		keyPanel.add(gPower);
		
		
		
		JPanel encryptPanel = new JPanel();
		
		JTextField keyField = new JTextField(20);
		encryptPanel.add(keyField);
		
		JTextField messageField = new JTextField(20);
		encryptPanel.add(messageField);
		
		JTextField messageLabel = new JTextField();
		messageLabel.setEditable(false);
		JButton encrypt = new JButton("Encrypt Message");
		JButton decrypt = new JButton("Decrypt Message");
		encrypt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				messageLabel.setText(encrypt(messageField.getText(), keyField.getText()));
			}
		});
		decrypt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				messageLabel.setText(decrypt(messageField.getText(), keyField.getText()));
			}
		});
		encryptPanel.add(encrypt);
		encryptPanel.add(decrypt);
		encryptPanel.add(messageLabel);
		
		
		
		
		
		keyPanel.setBackground(this.getBackground());
		encryptPanel.setBackground(this.getBackground());
		this.add(keyPanel);
		this.add(encryptPanel);
	}
	
	private String encrypt(String message, String stringKey){
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			byte[] bytes = new byte[16];
			System.arraycopy(new BigInteger(stringKey).toByteArray(), 0, bytes, 0, Math.min(bytes.length,new BigInteger(stringKey).toByteArray().length));
			SecretKeySpec key = new SecretKeySpec(bytes, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return Base64.encodeBase64String(cipher.doFinal(message.getBytes()));
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalBlockSizeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (BadPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	private String decrypt(String message, String stringKey){
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			byte[] bytes = new byte[16];
			System.arraycopy(new BigInteger(stringKey).toByteArray(), 0, bytes, 0, Math.min(bytes.length,new BigInteger(stringKey).toByteArray().length));
			SecretKeySpec key = new SecretKeySpec(bytes, "AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return new String(cipher.doFinal(Base64.decodeBase64(message)));
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalBlockSizeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (BadPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return null;
	}
	
}
