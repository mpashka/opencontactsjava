package org.mpn.contacts.importer;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.DiscoverInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Test JEP-100 spec
 * 
 * @author Noah Campbell
 * @version 1.0
 * @see "JEP-100"
 */
public class JEP_100 {

    
    /** The GATEWAY. */
    private static final String GATEWAY = "testloopback";

    
    /** The logger. */
    private static Logger logger = Logger.getLogger("JEP_100");
    
    
    /** The connection. */
    private XMPPConnection connection;
    
    /** The domain. */
    private static String domain = "ncampbel01";
    
    /** The username. */
    private String username= "test" + System.nanoTime();
    
    /** The password. */
    private String password= "test";
    
    
    /** The friend. */
    private String friend = "noahsingleton";
    
    /** The friendName. */
    private String friendName = "Noah";
    
    
    /** The yahooName. */
    private String yahooName = "test";
    
    /** The yahooCredential. */
    private String yahooCredential = "test";
    
    
    /** The testGateway. @see TestLoopbackGateway */
//    private TestLoopbackGateway testGateway;

    
    /** The aFriend. */
    private static final String aFriend = "bubba@" + GATEWAY + "." + domain;

    
    
    /** The manager. */
//    private ExternalComponentManager manager = new ExternalComponentManager("ncampbel01", 10015);
    
    
    
    /**
     * Bootstrap code for the test case.
     * @throws Exception
     */
/*
    @Configuration(beforeTestClass = true)
    public void bootstrap() throws Exception {
        logger.info("bootstrap");
        ComponentManagerFactory.setComponentManager(manager);
        manager.setSecretKey(GATEWAY, "testing");
        testGateway = new TestLoopbackGateway();
        manager.addComponent(GATEWAY, testGateway);
    }
*/

    
    /**
     * Teardown anything configuration that have been setup in the <code>bootstrap</code>.
     * @throws Exception
     */
/*
    @Configuration(afterTestClass = true)
    public void teardown() throws Exception {
        logger.info("teardown");
        manager.removeComponent(GATEWAY);
    }
*/

    /**
     * JEP 100 4.1.1 Sequence.  Basic Registration
     * @throws Exception
     */
//    @Test(groups={"bootstrap"})
    public void JEP100_411_registration() throws Exception {
        
        logger.info("Register");
        connection = new XMPPConnection(domain);
        
        try {
            connection.getAccountManager().createAccount(username, password);
            connection.disconnect();
            connection = new XMPPConnection(domain);
        } catch (XMPPException e) {
            logger.log(Level.INFO, "Unable to create account", e);
            assert false : "Unable to create account";
        }
        
        connection.login(username, password);
        
        
        // 4.1.1.1 Jabber User sends IQ get qualified by the Service Discovery
        ServiceDiscoveryManager discoManager = new ServiceDiscoveryManager(connection);
        assert discoManager != null;
        
        // 4.1.1.2 Gateway and/or parent returns identity information to Jabber User's Client
        DiscoverInfo info = discoManager.discoverInfo(GATEWAY + "." + domain);
        assert info.containsFeature("jabber:iq:register");
        
        PacketCollector collector = connection.createPacketCollector(new PacketFilter() {
            public boolean accept(Packet arg0) {
                return arg0 instanceof Registration;
            }});
        
        // 4.1.1.3 Jabber User sends IQ get qualified by the In-Band Registration
        Registration conv1 = new Registration();
        conv1.setType(IQ.Type.GET);
        conv1.setTo(GATEWAY + "." + domain);
        connection.sendPacket(conv1);
        
        // 4.1.1.4 Gateway returns IQ result to Jabber User, specifying information that 
        //  is required in order to register
        Registration response = (Registration)collector.nextResult();

        // 4.1.1.5 Jabber User sends IQ set qualified by the 'jabber:iq:register' namespace 
        //  to Gateway
        Registration regRegister = new Registration();
        regRegister.setType(IQ.Type.SET);
        regRegister.setTo(response.getFrom());
        
        DataForm df = new DataForm("submit");
        FormField pwdField = new FormField("password");
        pwdField.addValue(this.yahooCredential);
        FormField usrField = new FormField("username");
        usrField.addValue(this.yahooName);
        
        df.addField(pwdField);
        df.addField(usrField);
        regRegister.addExtension(df);
        
        PacketCollector subscription = connection.createPacketCollector(new PacketTypeFilter(Presence.class));
        
        connection.sendPacket(regRegister);
        
        // 4.1.1.6 Gateway verifies that registration information provided by Jabber User is valid
        response = (Registration) collector.nextResult();
        assert response != null;
        XMPPError error = response.getError();
        if(error != null) {
            logger.severe(error.toString());
            assert false : "Invalid respones " + error.toString();
        }
        
        // 4.1.1.7 Gateway buffers any translatable events 
        //  ...happens on the gateway...
        
        // TODO 4.1.1.8 Optionally, Jabber User sends IQ set qualified by the 'jabber:iq:roster' 
        //  namespace to its server
        
        // 4.1.1.9 Gateway sends subscription request to Jabber User
        while(true) {
            Presence p = (Presence) subscription.nextResult(5 * 1000);
            assert p != null;
            logger.fine(p.toXML());
            if(p.getType().equals(Presence.Type.subscribe)) {
                
                // 4.1.1.10 Jabber User's client SHOULD approve the subscription request
                Presence subscribed = new Presence(Presence.Type.subscribed);
                subscribed.setTo(p.getFrom());
                connection.sendPacket(subscribed);
                break;
            }
        }
        
        // 4.1.1.11 Jabber User sends subscription request to Gateway
        Presence gatewaySub = new Presence(Presence.Type.subscribe);
        gatewaySub.setTo(GATEWAY + "."  + domain);
        connection.sendPacket(gatewaySub);
        
        // 4.1.1.12 Gateway sends approves subscription request
        while(true) {
            Presence p = (Presence) subscription.nextResult(10 * 1000);
            assert p != null;
            if(p.getType().equals(Presence.Type.subscribed)) {
                break;
            }
        }
    }
    
    /**
     * JEP 100 4.3 Unregistration
     * @throws Exception 
     */
//    @Test(groups={"teardown"},dependsOnGroups={"cleanup"})
    @SuppressWarnings("unchecked")
    public void JEP100_431_unregister() throws Exception {
        // 4.3.1.1 Jabber User sends IQ set in 'jabber:iq:register' namespace to Gateway, 
        //  containing empty <remove/> element
        Registration conv1 = new Registration();
        Map attr = new HashMap();
        attr.put("remove", null);
        conv1.setAttributes(attr);
        conv1.setTo(GATEWAY + "."  + domain);

        PacketCollector resultCollector = connection.createPacketCollector(
                new PacketIDFilter(conv1.getPacketID()));
        PacketCollector presenceCollector = connection.createPacketCollector(
                new PacketTypeFilter(Presence.class));
        
        connection.sendPacket(conv1);
        
        // 4.3.1.2 and 4.3.1.3 happen on the server
        
        // 4.3.1.4 Gateway sends IQ result to Jabber User
        Packet response = resultCollector.nextResult();
        assert response != null;
        
        boolean unsubscribed = false;
        boolean unsubscribe = false;
        boolean unavailable = false;
        

        while(!unsubscribe || !unsubscribed) {
            Presence p = (Presence)presenceCollector.nextResult(10 * 1000);
            assert p != null : "Missing presence packet. State[unsubcribe: " + unsubscribe + ", unsubscribed: " + unsubscribed + ", unavailable: " + unavailable + "]";
            
            // 4.3.1.5 Gateway cancels subscriptions
            if(!unsubscribe && p.getType().equals(Presence.Type.unsubscribe)) {
                unsubscribe = true;
            }
            
            if(!unsubscribed && p.getType().equals(Presence.Type.unsubscribed)) {
                unsubscribed = true;
            }

            // 4.3.1.6 Gateway sends unavailable presence to Jabber User
            if(!unavailable && p.getType().equals(Presence.Type.unavailable)) {
                unavailable = true;
            }
        }
        
        if(!unavailable) {
            logger.info("Unable to get UNAVAILABLE...JM does not forward those type of requests");
        }
        
        presenceCollector.cancel();
        resultCollector.cancel();
        
        connection.getAccountManager().deleteAccount();
        connection.disconnect();
        connection = null;
    }
    
    /**
     * login
     * @throws Exception
     */
//    @Test(dependsOnGroups={"bootstrap"})
    public void JEP100_441_login() throws Exception {
        Presence p = new Presence(Presence.Type.available);
        p.setTo(GATEWAY + "." + domain);
        connection.sendPacket(p);
    }
    
    /**
     * JEP-100 4.5 Log Out
     * @throws Exception 
     */
//    @Test(groups={"cleanup"})
    public void JEP100_451_logout() throws Exception {
        // 4.5.1 Jabber User sends unavailable presence broadcast to Server
        Presence unavailable = new Presence(Presence.Type.unavailable);
        unavailable.setTo(GATEWAY + "."  + domain);
        PacketCollector collector = connection.createPacketCollector(new PacketTypeFilter(Presence.class));
        connection.sendPacket(unavailable);
        boolean loggedOut = false;
        while(!loggedOut) {
            Presence p = (Presence) collector.nextResult(5 * 1000);
            if(p == null) {
                logger.fine("Unable to get UNAVAILABLE packet...this may be due to JM not publishing the response");
                break;
            }
            if(p.getType().equals(Presence.Type.unavailable)) {
                loggedOut = true;
            }            
        }
    }
    
 
    /**
     * Add a contact
     * 
     * @throws Exception
     */
//    @Test(groups={"setup"}, dependsOnGroups={"bootstrap"})
    public void JEP100_461_addContact() throws Exception {
        Roster roster = connection.getRoster();
 
        String absoluteFriend = friend + "@" + GATEWAY + "." + domain; // ;)
 
        // add entry
        roster.createEntry(absoluteFriend, friendName, new String[] {"pals"});

        Thread.sleep(5 * 1000); // allow for some time to pass
        // inspect results
        RosterEntry entry = roster.getEntry(absoluteFriend);
        assert entry != null;
        assert entry.getType() == RosterPacket.ItemType.both;
    }
    
    /**
     * JEP 100 4.7.1 Delete Contact
     * 
     * Jabber User sends IQ set qualified by the 'jabber:iq:roster' namespace, containing subscription 
     * attribute with value of "remove".
     * @throws Exception 
     */
//    @Test(groups={"cleanup"},dependsOnMethods={"JEP100_461_addContact"})
    public void JEP100_471_deleteContact() throws Exception {
        Roster roster = connection.getRoster();
        String absoluteFriend = friend + "@" + GATEWAY + "." + domain;
        
        RosterEntry entry = roster.getEntry(absoluteFriend);
        roster.removeEntry(entry);
        
        Thread.sleep(5 * 1000);
        assert roster.getEntry(absoluteFriend) == null;
    }
    
    /**
     * JEP 100 4.8.1 Naturally, the Jabber User may want to exchange messages 
     * with a Legacy User.
     * @throws Exception
     */
//    @Test(groups={"nominal"}, dependsOnMethods={"JEP100_511_legacyAddContact"},
//            dependsOnGroups={"setup.*"})
    public void JEP100_481_sendMessage() throws Exception {
//        Chat chat = connection.getChatManager().createChat(aFriend);
//        chat.sendMessage("Neither, fair saint, if either thee dislike.");
//        Message message = chat.nextMessage(5 * 1000);
//        assert message != null;
//        assert message.getBody() != null;
//        assert message.getBody().equalsIgnoreCase("Neither, fair saint, if either thee dislike.");
    }
    
    /**
     * JEP 100 5.1.1 Legacy user register
     * 
     * The Legacy User may want to add the Jabber User to his or her contact 
     * list on the Legacy Service.
     * @throws Exception
     */
//    @Test(groups = {"legacy", "setup"}, dependsOnGroups={"bootstrap"})
    public void JEP100_511_legacyAddContact() throws Exception {
//        TestLoopbackGateway.TEST_SESSION.legacyAddContact("bubba");
//        Roster roster = connection.getRoster();
//
//        RosterListener listener = new RosterListener() {
//
//            public void rosterModified() {
//                logger.info("Roster Modified");
//            }
//
//            public void presenceChanged(String arg0) {
//                logger.info("Presence Changed: " + arg0);
//
//            }};
//        roster.addRosterListener(listener);
//
//        Thread.sleep(2 * 1000);
//        roster.removeRosterListener(listener);
//        RosterEntry entry = roster.getEntry(aFriend);
//        assert entry != null;
//        assert entry.getType().equals(RosterPacket.ItemType.from);
//
//        Presence subscribe = new Presence(Presence.Type.subscribe);
//        subscribe.setTo(aFriend);
//        subscribe.setFrom(connection.getUser());
//        connection.sendPacket(subscribe);
//
//        Thread.sleep(2 * 1000);
//
//        assert entry.getType().equals(RosterPacket.ItemType.both);
        
    }
    
    /**
     * JEP 100 5.2 Legacy Delete User
     * 
     * After adding the Jabber User to his or her legacy contact list, the 
     * Legacy User may want to delete the Jabber User.
     * 
     * @throws Exception
     */
//    @Test(groups = {"legacy", "cleanup"}, dependsOnMethods = {"JEP100_511_legacyAddContact"})
    public void JEP100_521_legacyRemoveContact() throws Exception {
//        TestLoopbackGateway.TEST_SESSION.legacyRemoveContact("bubba");
//        Thread.sleep(5 * 1000);
//
//        Roster roster = connection.getRoster();
//        assert roster.getPresence(aFriend) == null;
    }
    
    /**
     * JEP 100 5.3.1
     * 
     * Naturally, the Legacy User may want to exchange messages with the Jabber 
     * User.
     * @throws Exception 
     * 
     */
//    @Test(groups={"nominal"},dependsOnMethods={"JEP100_511_legacyAddContact"},
//            dependsOnGroups={"setup"})
    public void JEP100_531_legacySendMessage() throws Exception {
//        Chat c = connection.createChat(aFriend);
//
//        TestLoopbackGateway.TEST_SESSION.legacySendMessage(aFriend, "legacy messages rock", c.getThreadID());
//        Message m = c.nextMessage(5 * 1000);
//        assert m != null;
//        assert m.getBody() != null;
//        assert m.getBody().equals("legacy messages rock");
    }

}
