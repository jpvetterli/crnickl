/*
 *   Copyright 2012 Hauser Olsson GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Package: ch.agent.crnickl
 * Type: T2DBMsg
 * Version: 1.0.0
 */
package ch.agent.crnickl;

import java.util.ResourceBundle;

import ch.agent.core.KeyedMessage;
import ch.agent.core.MessageBundle;

/**
 * T2DBMsg provides keyed messages generated in CrNiCKL.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class T2DBMsg extends KeyedMessage {

	/**
	 * Message symbols.
	 */
	public class D {
		public static final String D00101 = "D00101";
		public static final String D00102 = "D00102";
		public static final String D00103 = "D00103";
		public static final String D00104 = "D00104";
		public static final String D00105 = "D00105";
		public static final String D00108 = "D00108";
		public static final String D00109 = "D00109";
		public static final String D00110 = "D00110";
		public static final String D00111 = "D00111";
		public static final String D00112 = "D00112";
		public static final String D00113 = "D00113";
		public static final String D00114 = "D00114";
		public static final String D00115 = "D00115";
		public static final String D00116 = "D00116";
		public static final String D00121 = "D00121";

		public static final String D01102 = "D01102";
		public static final String D01103 = "D01103";
		public static final String D01104 = "D01104";
		
		public static final String D10101 = "D10101";
		public static final String D10102 = "D10102";
		public static final String D10103 = "D10103";
		public static final String D10104 = "D10104";
		public static final String D10105 = "D10105";
		public static final String D10107 = "D10107";
		public static final String D10108 = "D10108";
		public static final String D10110 = "D10110";
		public static final String D10114 = "D10114";
		public static final String D10115 = "D10115";
		public static final String D10119 = "D10119";
		public static final String D10120 = "D10120";
		public static final String D10121 = "D10121";
		public static final String D10122 = "D10122";
		public static final String D10123 = "D10123";
		
		public static final String D02102 = "D02102";
		public static final String D02103 = "D02103";
		
		public static final String D20107 = "D20107";
		public static final String D20108 = "D20108";
		public static final String D20109 = "D20109";
		public static final String D20110 = "D20110";

		public static final String D30105 = "D30105";
		public static final String D30106 = "D30106";
		public static final String D30108 = "D30108";
		public static final String D30109 = "D30109";
		public static final String D30110 = "D30110";
		public static final String D30111 = "D30111";
		public static final String D30112 = "D30112";
		public static final String D30113 = "D30113";
		public static final String D30114 = "D30114";
		public static final String D30115 = "D30115";
		public static final String D30117 = "D30117";
		public static final String D30118 = "D30118";
		public static final String D30119 = "D30119";
		public static final String D30120 = "D30120";
		public static final String D30121 = "D30121";
		public static final String D30122 = "D30122";
		public static final String D30123 = "D30123";
		public static final String D30124 = "D30124";
		public static final String D30125 = "D30125";
		public static final String D30126 = "D30126";
		public static final String D30127 = "D30127";
		public static final String D30128 = "D30128";
		public static final String D30130 = "D30130";
		public static final String D30132 = "D30132";
		public static final String D30133 = "D30133";
		public static final String D30135 = "D30135";
		public static final String D30136 = "D30136";
		public static final String D30137 = "D30137";
		public static final String D30138 = "D30138";
		public static final String D30140 = "D30140";
		public static final String D30141 = "D30141";
		public static final String D30146 = "D30146";
		public static final String D30148 = "D30148";
		public static final String D30149 = "D30149";
		public static final String D30150 = "D30150";
		public static final String D30151 = "D30151";
		public static final String D30152 = "D30152";
		public static final String D30153 = "D30153";
		
		public static final String D40101 = "D40101";
		public static final String D40102 = "D40102";
		public static final String D40103 = "D40103";
		public static final String D40104 = "D40104";
		public static final String D40108 = "D40108";
		public static final String D40107 = "D40107";
		public static final String D40109 = "D40109";
		public static final String D40110 = "D40110";
		public static final String D40111 = "D40111";
		public static final String D40114 = "D40114";
		public static final String D40115 = "D40115";
		public static final String D40126 = "D40126";
		public static final String D40127 = "D40127";
		public static final String D40130 = "D40130";
		public static final String D40504 = "D40504";
		public static final String D40505 = "D40505";
		public static final String D40506 = "D40506";
		
		public static final String D50101 = "D50101";
		public static final String D50102 = "D50102";
		public static final String D50106 = "D50106";
		public static final String D50107 = "D50107";
		public static final String D50108 = "D50108";
		public static final String D50109 = "D50109";
		public static final String D50110 = "D50110";
		public static final String D50111 = "D50111";
		public static final String D50115 = "D50115";
		public static final String D50116 = "D50116";
		public static final String D50130 = "D50130";
	}
	
	private static final String BUNDLE_NAME = ch.agent.crnickl.T2DBMsg.class.getName();
	
	private static final MessageBundle BUNDLE = new MessageBundle("T2DB",
			ResourceBundle.getBundle(BUNDLE_NAME));

	/**
	 * Return a keyed exception.
	 *  
	 * @param key a message key
	 * @param arg zero or more message arguments
	 * @return a keyed exception
	 */
	public static T2DBException exception(String key, Object... arg) {
		return new T2DBException(new T2DBMsg(key, arg));
	}

	/**
	 * Return a keyed exception.
	 * 
	 * @param cause the exception's cause
	 * @param key a message key
	 * @param arg zero or more message arguments
	 * @return a keyed exception
	 */
	public static T2DBException exception(Throwable cause, String key, Object... arg) {
		return new T2DBException(new T2DBMsg(key, arg), cause);
	}

	/**
	 * Construct a keyed message.
	 * 
	 * @param key a message key
	 * @param args zero or more message arguments
	 */
	public T2DBMsg(String key, Object... args) {
		super(key, BUNDLE, args);
	}

}
