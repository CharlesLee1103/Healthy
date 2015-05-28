package com.healthy.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.healthy.logic.ExtensionalIQ;
import com.healthy.logic.ExtensionalIQProvider;

import static com.healthy.util.Constants.*;

/**
 * Healthy�������ͨѶ�Ĺ�����,����
 * 
 * @author Kunlun Zhao
 * */
public class HealthyUtil implements ConnectionListener, PacketListener,
		PacketFilter {

	private static String DEBUG_TAG = "HEALTHY";
	private XMPPConnection mConnection;
	private SimpleDateFormat mDateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd kk:mm:ss");
	private static HealthyUtil mInstance;
	private List<SubscribePacketListener> mSubscribePacketListeners = new ArrayList<SubscribePacketListener>();

	private HealthyUtil() {
		config(ProviderManager.getInstance());// �����ӽ���֮ǰ������һЩ��Ҫ�����ò���
	}

	public synchronized static HealthyUtil getInstance() {
		if (mInstance == null)
			mInstance = new HealthyUtil();
		return mInstance;
	}

	/**
	 * ��ȡ�Ѿ����ӵ��ķ�����������
	 * 
	 * @throws HealthyException
	 * */
	public String getServerName() throws HealthyException {
		if (!isConnected())
			throw new HealthyException("��Ч��������");
		return mConnection.getServiceName();
	}

	/**
	 * �û���¼
	 * 
	 * @param name
	 *            �û���
	 * @param password
	 *            ����
	 * @throws HealthyException
	 * */
	public void login(String name, String password) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("��Ч��������");
		if (mConnection.isAuthenticated())
			return;
		try {
			/** ��¼ */
			mConnection.login(name, password);
		} catch (XMPPException e) {
			if (e.getXMPPError().getCode() == 401) {// not-authorized
				throw new HealthyException("�û�����������󣬵�¼ʧ��");
			}
			Log.e(DEBUG_TAG, "", e);
		}
	}

	/**
	 * �û�ע��
	 * 
	 * @param name
	 *            �û���
	 * @param password
	 *            ����
	 * @throws HealthyException
	 * */
	public void register(String name, String password) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("��Ч��������");
		Registration reg = new Registration();
		reg.setType(IQ.Type.SET);
		reg.setTo(mConnection.getServiceName());
		reg.setUsername(name);// ע������createAccountע��ʱ��������username������jid���ǡ�@��ǰ��Ĳ��֡�
		reg.setPassword(password);
		reg.addAttribute("android", "geolo_createUser_android");// ���addAttribute����Ϊ�գ������������������־��android�ֻ������İɣ���������
		PacketFilter filter = new AndFilter(new PacketIDFilter(
				reg.getPacketID()), new PacketTypeFilter(IQ.class));
		PacketCollector collector = mConnection.createPacketCollector(filter);
		mConnection.sendPacket(reg);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		collector.cancel();// ֹͣ����results���Ƿ�ɹ��Ľ����
		if (result == null) {// ���ֳ�ʱ
			throw new HealthyException("����ʱ");
		} else if (result.getType() == IQ.Type.RESULT) {// ע��ɹ�
			return;
		} else {
			if (result.getError().getCode() == 409) {// �Ѵ�������û�
				throw new HealthyException("�Ѵ�������û�");
			} else {
				throw new HealthyException("�ڲ�����������");
			}
		}
	}

	/**
	 * ��ȡ�ѵ�¼���û�����
	 * 
	 * @return the full XMPP address of the user logged in or null
	 * */
	public String getLoginedUser() {
		if (mConnection == null || !mConnection.isConnected())
			return null;
		return mConnection.getUser();
	}

	/**
	 * ��Ӻ���
	 * 
	 * @param name
	 *            jid (e.g. johndoe@jabber.org)
	 * @param nick
	 *            the nickname of the user
	 * 
	 * @throws HealthyException
	 * */
	public void addFriend(String name, String nick) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("��Ч��������");
		if (getLoginedUser() == null)
			throw new HealthyException("���Ƚ��е�¼");
		try {
			mConnection.getRoster().createEntry(name+"@"+mConnection.getServiceName(), nick, null);
			// ���붩�ĺ���״̬
			Presence subscription = new Presence(Presence.Type.subscribe);
			subscription.setTo(name);
			mConnection.sendPacket(subscription);
			Log.i("tag", "���������ѷ���");
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			throw new HealthyException(e.getXMPPError().getMessage());
		}
	}

	/**
	 * ɾ������
	 * 
	 * @param name
	 *            Ҫɾ���ĺ��ѵ�jid (e.g. johndoe@jabber.org)
	 * */
	public void removeFriend(String name) {
		RosterEntry entry = mConnection.getRoster().getEntry(name);
		try {
			mConnection.getRoster().removeEntry(entry);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, "", e);
		}
		// ȡ������
		Presence unsubscription = new Presence(Presence.Type.unsubscribe);
		unsubscription.setTo(name);
		mConnection.sendPacket(unsubscription);
	}

	/**
	 * ͬ���������
	 * 
	 * @param name
	 *            �����˵�jid (e.g. johndoe@jabber.org)
	 * */
	public void acceptFriendRequest(String name) {
		Presence presence = new Presence(Presence.Type.subscribed);
		presence.setTo(name+"@"+mConnection.getServiceName());
		mConnection.sendPacket(presence);
	}

	/**
	 * �ܾ���������
	 * 
	 * @param name
	 *            �����˵�jid (e.g. johndoe@jabber.org)
	 * */
	public void rejectFriendRequest(String name) {
		Presence presence = new Presence(Presence.Type.unsubscribed);
		presence.setTo(name+"@"+mConnection.getServiceName());
		mConnection.sendPacket(presence);
	}
	
	/**
	 * �Է�ͬ����Ӻ��ѣ�ȷ�Ϻ��ѹ�ϵ
	 * 
	 * @param name	�����˵�jid (e.g. johndoe@jabber.org)
	 */
	public void establishFriendRequest(String name)  {
		Presence presence = new Presence(Presence.Type.subscribed);
		presence.setTo(name+"@"+mConnection.getServiceName());
		mConnection.sendPacket(presence);
	}

	/**
	 * �ǳ���ǰ�û�
	 * */
	public void logout() {
		if (!isConnected())
			return;
		mConnection.disconnect();// �Ͽ����ӣ�����ʾ�ǳ�����
	}

	/**
	 * ��ȡ�����һ�����ں��ѵ�������������
	 * 
	 * @param p
	 *            ҳ��, ��ʼҳ��Ϊ0.
	 * @param psize
	 *            ÿҳ�Ĵ�С
	 * @param calories
	 *            ��ǰ�û��ڵ��µ���������
	 * 
	 * @return <b>String</b> �����б��Ѱ����������ĵĽ����������</br> ���ݽṹ:</br> {</br>
	 *         "count":�ɻ�õ����������ĵĺ���������</br> "friends":�����б�</br> [{</br>
	 *         "name":�����û���</br> "calories":������������</br> "time":
	 *         ��¼���һ�θ���ʱ��</br>},...]</br> }
	 * @throws com.healthy.util.HealthyUtil.HealthyException
	 * */
	public String getFriendsByCalories(int p, int psize, float calories)
			throws HealthyException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("category", RequestCode.GET_FRENDS_BY_CALORIES);// ��������
			jsonObject.put("p", p);
			jsonObject.put("psize", psize);
			jsonObject.put("calories", String.format("%.2f", calories));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, "", e);
		}
		return sendAMessageToServer(jsonObject.toString());
	}

	/**
	 * ���Ҹ������ˣ���һ���Ǻ��ѣ�
	 * 
	 * @param longitude
	 *            ����
	 * @param latitude
	 *            γ��
	 * @param radius
	 *            �����뾶
	 * @param p
	 *            ҳ��, ��ʼҳ��Ϊ0.
	 * @param psize
	 *            ÿҳ�Ĵ�С
	 * 
	 * @return <b>String</b> ���������б��Ѱ��������پ�������������� </br> ���ݽṹ:</br> {</br>
	 *         "count": ����������</br> "persons": �������б�</br> [{</br> "name":
	 *         �û���</br> "longitude": ����</br> "latitude": γ��</br> "lastUpdate":
	 *         ���һ�θ���λ��ʱ��</br>},...]</br> }
	 * 
	 * @throws com.healthy.util.HealthyUtil.HealthyException
	 * */
	public String getPersonsNearby(long longitude, long latitude, long radius,
			int p, int psize) throws HealthyException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("category", RequestCode.GET_PERSONS_NEARBY);// ��������
			jsonObject.put("p", p);
			jsonObject.put("psize", psize);
			jsonObject.put("longitude", longitude);
			jsonObject.put("latitude", latitude);
			jsonObject.put("radius", radius);
			jsonObject.put("time", mDateFormatter.format(new Date()));// ���һ�θ���ʱ��
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("Healthy", "", e);
		}
		return sendAMessageToServer(jsonObject.toString());
	}

	/**
	 * �����û�
	 * 
	 * @param keyword
	 *            Ҫ�����Ĺؼ���
	 * 
	 * @return �ɰ���keyword�û�������ɵ�String�б�
	 * 
	 * @throws HealthyException
	 * 
	 * */
	public List<String> searchUser(String keyword) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("��Ч��������");
		if (getLoginedUser() == null)
			throw new HealthyException("���Ƚ��е�¼");
		List<String> users = new ArrayList<String>();
		try {
			UserSearchManager manager = new UserSearchManager(mConnection);
			Form searchForm = manager.getSearchForm("search."
					+ mConnection.getServiceName());
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("search", keyword);
			ReportedData data = manager.getSearchResults(answerForm, "search."
					+ mConnection.getServiceName());
			Iterator<Row> iterator = data.getRows();
			while (iterator.hasNext()) {
				Row row = iterator.next();
				String user = row.getValues("Username").next().toString();
				users.add(user);
			}
		} catch (XMPPException e) {
			throw new HealthyException(e.getXMPPError().getMessage());
		}
		return users;
	}

	/**
	 * ��ȡ�û�VCard��Ϣ��VCard��������¼�û���Ϣ�Ľṹ��
	 * 
	 * @param name
	 *            �û�jid (e.g. johndoe@jabber.org)
	 * 
	 * @return vcard �û��˻���Ϣ
	 * 
	 * @throws HealthyException
	 * 
	 * @throws XMPPException
	 *             If no vCard exists or the user does not exist. The error code
	 *             is service-unavailable (503).
	 * */
	public VCard getUserVCard(String name) throws HealthyException,
			XMPPException {
		if (!isConnected())
			throw new HealthyException("��Ч��������");
		if (getLoginedUser() == null)
			throw new HealthyException("���Ƚ��е�¼");
		VCard vcard = new VCard();
		vcard.load(mConnection, name);
		return vcard;
	}

	/**
	 * ͨ��Vcard����ȡ�û�ͷ����Ϣ
	 * 
	 * @param name
	 *            �û�jid (e.g. johndoe@jabber.org)
	 * �����ʽΪ name@����--����mConnection.getServiceName()
	 * @throws XMPPException
	 *             If no vCard exists or the user does not exist. The error code
	 *             is service-unavailable (503).
	 * @throws HealthyException
	 * */
	public Bitmap getUserAvatar(String name) throws XMPPException,
			HealthyException {
		if (!isConnected())
			throw new HealthyException("��Ч��������");
		if (getLoginedUser() == null)
			throw new HealthyException("���Ƚ��е�¼");
		ByteArrayInputStream bais = null;
		VCard vcard = new VCard();
		vcard.load(mConnection, name+"@"+mConnection.getServiceName());
		if (vcard == null || vcard.getAvatar() == null)
			return null;
		bais = new ByteArrayInputStream(vcard.getAvatar());
		Bitmap avatar = BitmapFactory.decodeStream(bais);
		return avatar;
	}

	/**
	 * �ϴ��û�ͷ����Ϣ
	 * 
	 * @param avatar
	 *            �û�ͷ��������,����ͨ��openRawResource(int id)����ȡ�����ļ����������ļ�
	 * 
	 * @throws HealthyException
	 * @throws XMPPException
	 * @throws IOException
	 * 
	 * */
	public void uploadUserAvatra(InputStream avatar) throws HealthyException,
			XMPPException, IOException {
		if (!isConnected())
			throw new HealthyException("��Ч��������");
		if (getLoginedUser() == null)
			throw new HealthyException("���Ƚ��е�¼");
		VCard vcard = new VCard();
		vcard.load(mConnection);
		byte[] bytes = getBytesFromInputStream(avatar);
		vcard.setAvatar(bytes);
		vcard.save(mConnection);
	}

	/*
	 * ====================================˽�к���==================================
	 * ====
	 */

	/**
	 * ��������ת��Ϊbyte����
	 * */
	private byte[] getBytesFromInputStream(InputStream in) {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			while ((len = in.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			byte[] result = outStream.toByteArray();
			outStream.close();
			in.close();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, "", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �ڳ�ʼ������ʱ������һЩ��Ҫ�����ò���
	 * 
	 * */
	private void config(ProviderManager pm) {
		// �Զ���IQ
		pm.addIQProvider(ExtensionalIQ.ELEMENT, ExtensionalIQ.NAME_SPACE,
				new ExtensionalIQProvider());
		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
	}

	/**
	 * �����ַ���ĳ���ڵ��ڵ���Ϣ
	 * 
	 * @param source
	 *            ��Ҫ�������ַ���
	 * @param nodeName
	 *            �ڵ�����
	 * */
	private String parseXMLByDOM(String source, String nodeName) {
		try {
			DocumentBuilderFactory domFac = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder domBuilder = domFac.newDocumentBuilder();
			Document doc = domBuilder.parse(new ByteArrayInputStream(source
					.getBytes()));
			Element root = doc.getDocumentElement();
			return root.getElementsByTagName(nodeName).item(0).getTextContent();
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "", e);
		}
		return null;
	}

	private String sendAMessageToServer(String message) throws HealthyException {
		if (!isConnected())
			throw new HealthyException("��Ч��������");
		ExtensionalIQ iq = new ExtensionalIQ();
		iq.setMessage(message);
		iq.setType(IQ.Type.GET);
		iq.setTo(mConnection.getServiceName());
		mConnection.sendPacket(iq);
		PacketFilter filter = new AndFilter(
				new PacketIDFilter(iq.getPacketID()), new PacketTypeFilter(
						IQ.class));
		PacketCollector collector = mConnection.createPacketCollector(filter);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		collector.cancel();
		if (result == null) {// ������ֳ�ʱ
			throw new HealthyException("����ʱ");
		} else if (result.getType() == IQ.Type.RESULT) {// ����ִ�гɹ�
			return parseXMLByDOM(result.getChildElementXML(), "message");
		} else {
			throw new HealthyException("�ڲ�����������");
		}
	}

	/**
	 * ���ӵ�������
	 * */
	private boolean connectToServer() {
		ConnectionConfiguration config = new ConnectionConfiguration(
				"192.168.118.188", 5222);
		/* �Ƿ����ð�ȫ��֤ */
		config.setSASLAuthenticationEnabled(false);
		config.setReconnectionAllowed(true);// �����Զ���������
		/* ����connection���� */
		try {
			if (mConnection == null)
				mConnection = new XMPPConnection(config);
			mConnection.connect();/* �������� */
			mConnection.addConnectionListener(this);
			mConnection.addPacketListener(this, this);
			Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);// �趨Ĭ�Ϻ������ģʽ����Ҫ�����˹�ͬ��
			return true;
		} catch (XMPPException e) {
			Log.e("Healthy", "HealthyUtil.connectToServer():", e);
		}
		return false;
	}

	/**
	 * �ж��Ƿ��Ѿ��������ӣ���û�������½������ӡ�
	 * */
	private boolean isConnected() {
		if (mConnection == null || !mConnection.isConnected()) {
			if (!connectToServer()) {
				return false;
			}
		}
		return true;
	}

	/*
	 * ====================================�ӿ�====================================
	 * ==
	 */

	/**
	 * Notification that the connection was closed normally or that the
	 * reconnection process has been aborted.
	 * */
	@Override
	public void connectionClosed() {
		// TODO Auto-generated method stub
		Log.i(DEBUG_TAG, "Healthy�����˵������ѱ������ر�");
		mConnection = null;
	}

	/**
	 * Notification that the connection was closed due to an exception.
	 * */
	@Override
	public void connectionClosedOnError(Exception arg0) {
		// TODO Auto-generated method stub
		Log.e(DEBUG_TAG, "", arg0);
		connectToServer();// �������ӷ�����
	}

	/**
	 * The connection will retry to reconnect in the specified number of
	 * seconds.
	 * */
	@Override
	public void reconnectingIn(int arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * An attempt to connect to the server has failed.
	 * */
	@Override
	public void reconnectionFailed(Exception arg0) {
		// TODO Auto-generated method stub
		Log.e(DEBUG_TAG, "", arg0);
	}

	/**
	 * The connection has reconnected successfully to the server.
	 * */
	@Override
	public void reconnectionSuccessful() {
		// TODO Auto-generated method stub
		Log.i(DEBUG_TAG, "�ɹ����������");
	}

	/**
	 * �������������Ϣ
	 * */
	@Override
	public void processPacket(Packet packet) {
		// TODO Auto-generated method stub
		Presence presence = (Presence) packet;
		if (presence.getType() == Presence.Type.subscribe) {// �����������
			for (int i = 0; i < mSubscribePacketListeners.size(); i++)
				mSubscribePacketListeners.get(i).processPacket(packet);
		} else if (presence.getType() == Presence.Type.unsubscribe) {// ����ɾ������
			RosterEntry entry = mConnection.getRoster().getEntry(
					packet.getFrom());
			if (entry != null) {
				try {
					mConnection.getRoster().removeEntry(entry);
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					Log.e(DEBUG_TAG, "", e);
				}
			}
			// ����ȷ����Ϣ
			Presence unsubscription = new Presence(Presence.Type.unsubscribed);
			unsubscription.setTo(presence.getFrom());
			mConnection.sendPacket(unsubscription);
		} else if(presence.getType() == Presence.Type.subscribed){//�Է��ѽ��ܺ���������뷴����Ϣ
			Log.i("tag", "�õ����ѵ�ȷ����Ϣ");
			String[] messageFrom = packet.getFrom().split("@");
			String mMessageFrom = messageFrom[0];
			Log.i("tag", "�õ����ѵ�ȷ����Ϣ"+messageFrom[0]);
			establishFriendRequest(mMessageFrom);
			
		}
	}

	/**
	 * PacketFilter
	 * 
	 * ������������ѵ������ͨ��
	 * */
	@Override
	public boolean accept(Packet packet) {
		// TODO Auto-generated method stub
		if (packet instanceof Presence) {
			Presence presence = (Presence) packet;
			// Request subscription to recipient's presence.
			if (presence.getType() == Presence.Type.subscribe || // �����������
					presence.getType() == Presence.Type.unsubscribe||// ����ɾ������
						presence.getType() == Presence.Type.subscribed)//�ѽ��ܺ����������ķ�����Ϣ
				return true;
		}
		return false;
	}

	/**
	 * �����������ӿ�</br>
	 * 
	 * ��ؽ�����ʵ�ָýӿ�
	 * */
	public static interface SubscribePacketListener {
		public void processPacket(Packet packet);
	}
	
	public void addSubscribePacketListtener(SubscribePacketListener listener) {
		mSubscribePacketListeners.add(listener);
	}
}
