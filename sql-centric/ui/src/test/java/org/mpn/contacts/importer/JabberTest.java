package org.mpn.contacts.importer;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * Created by IntelliJ IDEA.
 * User: moukhataevs
 * Date: 26.12.2008
 * Time: 12:31:00
 * To change this template use File | Settings | File Templates.
 */
public class JabberTest {

    enum TestType {addGateway, removeGateway, addRemoveGateway, removeUser, allServers};

    static final Logger log = Logger.getLogger(JabberTest.class);

    private static final String JABBER_SERVER = "talk.google.com";
    private static final int JABBER_PORT = 5222;
    private static final String JABBER_SERVICE = "gmail.com";
    private static final boolean JABBER_SASL_AUTHENTICATION_ENABLED = true;

    private static final TestProperties TEST_PROPERTIES = TestProperties.getInstance();

    private static final String JABBER_CLIENT = "javatest";
    private static final String JABBER_LOGIN = TEST_PROPERTIES.getGoogleLogin() + "@gmail.com";
    private static final String JABBER_PASSWORD = TEST_PROPERTIES.getGooglePassword();


    private static final String ICQ_GATEWAY = "icq.jabber.spbu.ru";

    private static final String[] ICQ_GATEWAYS = {
"icq.chaoslab.info", 	 "icq.13.net.ru", 	 "icq.deac.ru",
"icq.geeklife.ru", 	"icq.aftar.ru", 	"icq2.jabber.krastalk.ru",
"icq.gelf.no-ip.org", 	"icq.blasux.ru",
"icq.informjust.ua", 	"icq.calypso.cn.ua",
"icq.jabbe.net.ru", 	"icq.deac.ru",
"icq.jabber.corbina.ru", 	"icq.freeside.ru",
"icq.jabber.cv.ua", 	"icq.gornyak.net",
"icq.jabber.crimea.ua", 	"icq.highsecure.ru",
"icq.jabber.krasu.ru", 	"icq.intramail.ru",
"icq.jabber.org.ru", 	"icq.jabber.chirt.ru",
"icq.jabber.spbu.ru", 	"icq.jabber.ck.ua",
"icq.jabber.splc.ru", 	"icq.jabber.kiev.ua",
"icq.jabber.te.ua", 	"icq.jabber.lviv.ua",
"icq.jabber.ukrwest.net", 	"icq.jabber.nnov.net",
"icq.plotinka.ru", 	"icq.jabber.rfei.ru",
"icq.sgtp.samara.ru", 	"icq.jabber.rikt.ru",
"icq.tr.element.dn.ua", 	"icq.jabber.stniva.ru",
"pyicq.jabber.te.ua", 	"icq.mytlt.ru",
"icq.jabber.tsure.ru", 	"icq.jabber.uch.net",
"icqp.13.net.ru", 	"icq.jabe.ru",
"icq.jabber.b.gz.ru", 	"icq.kanet.ru",
"icq.jabber.fds-net.ru", 	"icq.mytlt.ru",
"icq.jabber.krastalk.ru", 	"icq.nclug.ru",
"icq.jmsk.legion.ru", 	"icq.ratelcom.ru",
"icq.jabber.kursk.lug.ru", 	"icq.smim.ru",
"icq.vlg.lukoil.ru", 	"icq.udaff.com",
"icq.myid.ru", 	"icq.jabber.sv.mh.ru",
"icq.ilikejabber.ru", 	"icq.medexport-omsk.ru",
"icq.jabber.nwg-nv.ru", 	"icq.jabe.ru",
"icq.mo.pp.ru", 	"icq.office.ksn.ru",
"icq.proc.ru",
"icq.ru","lezz.ru",
    };


    private static final String ICQ_LOGIN = TEST_PROPERTIES.getProperty("icq.login");
    private static final String ICQ_PASSWORD = TEST_PROPERTIES.getProperty("icq.password");

    private static final String ICQ_USER_NORM = TEST_PROPERTIES.getProperty("icq.user.norm");
    private static final String ICQ_USER_ADD_REMOVE = TEST_PROPERTIES.getProperty("icq.user.add.remove");

    private static final long REGISTER_TIMEOUT = 5000;
    private static final long REGISTER_DELAY = 500;

    private ServiceDiscoveryManager discoManager;

    @Test
    public void testJabber() throws IOException {
        try {
            doTestJabber(TestType.addGateway);
        } catch (Exception e) {
            log.error("Jabber Error", e);
        }
    }


    public void doTestJabber(TestType testType) throws Exception {

// Create a connection to the jabber.org server on a specific port.
        ConnectionConfiguration config = new ConnectionConfiguration(JABBER_SERVER, JABBER_PORT, JABBER_SERVICE);

        {
        config.setSASLAuthenticationEnabled(JABBER_SASL_AUTHENTICATION_ENABLED);
  // You have to put this code before you login

//            SASLAuthentication.supportSASLMechanism("PLAIN", 0);
        }
        
        final XMPPConnection conn = new XMPPConnection(config);

        conn.connect();

        // Obtain the ServiceDiscoveryManager associated with my XMPPConnection
        discoManager = ServiceDiscoveryManager.getInstanceFor(conn);

        conn.addConnectionListener(new ConnectionListener() {
            public void connectionClosed() {
                log.info("            Connection closed");
            }

            public void connectionClosedOnError(Exception e) {
                log.info("            Connection closed on error", e);
            }

            public void reconnectingIn(int seconds) {
                log.info("            ReConnecting...");
            }

            public void reconnectionSuccessful() {
                log.info("            ReConnecting ok");
            }

            public void reconnectionFailed(Exception e) {
                log.info("            ReConnecting error", e);
            }
        });


        conn.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                log.trace("                >>> Incoming packet : " + packet.getClass() + " -> " + packet.toXML());
                if (packet instanceof Presence) {
                    Presence presence = (Presence) packet;
                    if (presence.getType() == Presence.Type.subscribe) {
                        // Accept all subscription requests.
                        Presence response = new Presence(Presence.Type.subscribed);
                        response.setTo(presence.getFrom());
                        conn.sendPacket(response);
                        log.info("            Subscribed for " + presence.getFrom());
                    } else if (presence.getType() == Presence.Type.unsubscribe) {
                        // Acknowledge and accept unsubscription notification so that the
                        // server will stop sending notifications saying that the contact
                        // has unsubscribed to our presence.
                        Presence response = new Presence(Presence.Type.unsubscribed);
                        response.setTo(presence.getFrom());
                        conn.sendPacket(response);
                        // Otherwise, in manual mode so ignore.
                        log.info("            UnSubscribed for " + presence.getFrom());
                    } else {
                        log.debug("            Presence : " + presence);
                    }

/*
                } else if (packet instanceof DiscoverItems) {
                    DiscoverItems discoverItems = (DiscoverItems) packet;
                    log.info("Discover items responce...");
                    // Get the discovered items of the queried XMPP entity
                    for (Iterator<DiscoverItems.Item> it = discoverItems.getItems(); it.hasNext();) {
                        DiscoverItems.Item item = it.next();
                        log.info("    Item : " + item);
                    }
                    log.info("Discover items OK");
*/
//                } else if (packet instanceof IQ) {
//                    IQ packetIQ = (IQ) packet;
//                    log.info("IQ packet : " + packetIQ);
//                    log.info("IQ packet : " + packetIQ.getClass());
                }
            }
        }, null);
        conn.addPacketWriterListener(new PacketListener() {
            public void processPacket(Packet packet) {
                log.trace("                <<< Outgoing packet : " + packet.toXML());
            }
        }, null);

        conn.login(JABBER_LOGIN, JABBER_PASSWORD, JABBER_CLIENT);

        log.info("Login succesfull");
        log.info("    User : " + conn.getUser());


        Roster roster = conn.getRoster();
        roster.addRosterListener(new RosterListener() {
            // Ignored events
            public void entriesAdded(Collection<String> addresses) {
                log.info("            Roster Entries added : " + addresses);
            }
            public void entriesDeleted(Collection<String> addresses) {
                log.info("            Roster Entries deleted : " + addresses);
            }
            public void entriesUpdated(Collection<String> addresses) {
                log.info("            Roster Entries updated : " + addresses);
            }
            public void presenceChanged(Presence presence) {
                log.info("            Presence changed: " + presence.getFrom() + " " + presence);
            }
        });

        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);


        {
// Create a new presence. Pass in false to indicate we're unavailable.
        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus("Hello! I am here! И русский.");
// Send the packet (assume we have a XMPPConnection instance called "con").
        conn.sendPacket(presence);
        }




        if (false) {
            showRosterInfo(roster);

            log.info("Subscribe to self...");
            Presence response = new Presence(Presence.Type.subscribe);
            response.setTo(JABBER_LOGIN);
            conn.sendPacket(response);
//        RosterEntry rosterEntry = roster.getEntry(JABBER_LOGIN);
//        rosterEntry.


            {
                Chat chatLocalUser = conn.getChatManager().createChat(JABBER_LOGIN, new SimpleMessageListener());
                chatLocalUser.sendMessage("Self First message");
                chatLocalUser.sendMessage("Self Второе сообщение");
            }

            {
                Chat chatLocalUser = conn.getChatManager().createChat("pavel.moukhataev@gmail.com", new SimpleMessageListener());
                chatLocalUser.sendMessage("First message");
                chatLocalUser.sendMessage("Второе сообщение");
            }
        }
//        Thread.sleep(1000);


        if (true) {
            String gatewayJid = "icq.aftar.ru";

            discoverItems("aftar.ru", null);
            discoverIdentities(gatewayJid, null);

            if (true) return;

            discoverItems(JABBER_LOGIN, null);
//        discoverIdentities(JABBER_LOGIN, null);

//        registerOnGateway(conn, gatewayJid);

            logoutFromGateway(conn, "icq.freeside.ru");
            unregisterOnGateway(conn, "icq.freeside.ru");
            logoutFromGateway(conn, gatewayJid);
            unregisterOnGateway(conn, gatewayJid);

        }

        if (testType == TestType.allServers) {
            for (String icqGateway : ICQ_GATEWAYS) {
                try {
                    testAllIcqServers(conn, icqGateway);
                } catch (Exception e) {
                    log.error("Error testing " + icqGateway, e);
                }
            }

        } else {
            testCaseIcqMain(testType, conn, ICQ_GATEWAY);
        }

        waitPressKey("disconnect");

        conn.disconnect();
//        conn.disconnect(new Presence(Presence.Type.unavailable, "I am away. Я ушел...", 0, Presence.Mode.away));

        waitPressKey("EXIT");

        
    }

    /*
     * 1) Registers on gateway
     * 2) Add ICQ contacts
     * 3) Send authorization to ICQ_USER_ADD_REMOVE
     * 4) Send messages
     * 5) Remove user ICQ_USER_ADD_REMOVE
     * 6) Unregister on gateway
     *
     */
    private void testCaseIcqMain(TestType testType, XMPPConnection conn, String gatewayJid) throws Exception {
        log.info("-------------------------------------------------------------------------- ICQ test case");
        Roster roster = conn.getRoster();
        if (roster.getEntry(gatewayJid) == null) {
            registerOnGateway(conn, gatewayJid);
        }

        Presence gatewayPresence = roster.getPresence(gatewayJid);
        if (!gatewayPresence.isAvailable()) {
            loginToGateway(conn, gatewayJid);
        }

        showRosterInfo(roster);
        addContactIfAbsent(conn, gatewayJid, ICQ_USER_NORM);
        addContactIfAbsent(conn, gatewayJid, ICQ_USER_ADD_REMOVE);
        waitPressKey("authorize contacts1");
        addContactIfAbsent(conn, gatewayJid, ICQ_USER_NORM);
        addContactIfAbsent(conn, gatewayJid, ICQ_USER_ADD_REMOVE);
        waitPressKey("authorize contacts2");
        JEP100_481_sendMessage(conn, gatewayJid, ICQ_USER_NORM);
        JEP100_481_sendMessage(conn, gatewayJid, ICQ_USER_ADD_REMOVE);
        waitPressKey("chat");
        if (testType == TestType.removeUser) {
            JEP100_471_deleteContact(conn, gatewayJid, ICQ_USER_ADD_REMOVE);
        }

        if (testType == TestType.removeGateway) {
            waitPressKey("remove gateway");
            logoutFromGateway(conn, gatewayJid);
            unregisterOnGateway(conn, gatewayJid);

            removeAllIcqUsers(conn);
        }
    }

    /*
     * 1) Registers on gateway
     * 2) Add ICQ contacts
     * 3) Send authorization to ICQ_USER_ADD_REMOVE
     * 4) Send messages
     * 5) Remove user ICQ_USER_ADD_REMOVE
     * 6) Unregister on gateway
     *
     */
    private void testAllIcqServers(XMPPConnection conn, String gatewayJid) throws Exception {
        log.info("-------------------------------------------------------------------------- ICQ test case");
        Roster roster = conn.getRoster();
        // remove all previous gateways
        for (RosterEntry rosterEntry : roster.getEntries()) {
            if (!rosterEntry.getUser().contains("@")) {
                // removing
                String myGatewayJid = rosterEntry.getUser();
                logoutFromGateway(conn, myGatewayJid);
                unregisterOnGateway(conn, myGatewayJid);
            }
        }

        // remove all previous icq users
        removeAllIcqUsers(conn);

        if (roster.getEntry(gatewayJid) == null) {
            registerOnGateway(conn, gatewayJid);
        }

        Presence gatewayPresence = roster.getPresence(gatewayJid);
        if (!gatewayPresence.isAvailable()) {
            loginToGateway(conn, gatewayJid);
        }

        showRosterInfo(roster);
        addContactIfAbsent(conn, gatewayJid, ICQ_USER_NORM);
        long now = System.currentTimeMillis();
        long end = now + REGISTER_TIMEOUT;
        while (System.currentTimeMillis() < end && !addContactIfAbsent(conn, gatewayJid, ICQ_USER_NORM)) {
            Thread.sleep(REGISTER_DELAY);
        }
        if (!addContactIfAbsent(conn, gatewayJid, ICQ_USER_NORM)) {
            log.error("Can't contact user from gateway : " + gatewayJid);
        }
        JEP100_481_sendMessage(conn, ICQ_GATEWAY, ICQ_USER_NORM);

        waitPressKey("remove gateway");
        logoutFromGateway(conn, gatewayJid);
        unregisterOnGateway(conn, gatewayJid);

        removeAllIcqUsers(conn);
    }

    private void removeAllIcqUsers(XMPPConnection conn) throws XMPPException {
        log.info("Remove all icq users...");
        Roster roster = conn.getRoster();
        for (RosterEntry rosterEntry : roster.getEntries()) {
            if (rosterEntry.getUser().contains("icq")) {
                log.info("    Removing ICQ user " + rosterEntry.getName());
                roster.removeEntry(rosterEntry);
            }
        }
        log.info("Remove OK");
    }


    /*
     * 1) Registers on gateway
     * 2) Add ICQ contacts
     * 3) Send authorization to ICQ_USER_ADD_REMOVE
     * 4) Send messages
     * 5) Remove user ICQ_USER_ADD_REMOVE
     * 6) Unregister on gateway
     *
     */
    private void testCaseIcqNoRemove(XMPPConnection conn) throws Exception {
        log.info("-------------------------------------------------------------------------- ICQ test case");
        Roster roster = conn.getRoster();
        if (roster.getEntry(ICQ_GATEWAY) == null) {
            log.info("    Gateway was not registered. Registering ...");
            registerOnGateway(conn, ICQ_GATEWAY);
        }

        Presence gatewayPresence = roster.getPresence(ICQ_GATEWAY);
        if (!gatewayPresence.isAvailable()) {
            log.info("    Gateway was not Loggedin. Logging in ...");
            loginToGateway(conn, ICQ_GATEWAY);
        }


        log.info("----- Roster before add user");
        showRosterInfo(roster);
        addContactIfAbsent(conn, ICQ_GATEWAY, ICQ_USER_NORM);
        addContactIfAbsent(conn, ICQ_GATEWAY, ICQ_USER_ADD_REMOVE);
        log.info("----- Roster after add user");
        showRosterInfo(roster);

        waitPressKey("authorize contacts");
        JEP100_481_sendMessage(conn, ICQ_GATEWAY, ICQ_USER_NORM);
        JEP100_481_sendMessage(conn, ICQ_GATEWAY, ICQ_USER_ADD_REMOVE);
    }

    private boolean addContactIfAbsent(XMPPConnection conn, String gatewayJid, String icqUserID) throws Exception {
        Roster roster = conn.getRoster();
        String userJid = icqUserID + '@' + gatewayJid;
        RosterEntry icqUserNorm = roster.getEntry(userJid);
        if (icqUserNorm == null) {
            JEP100_461_addContact(conn, gatewayJid, icqUserID);
        } else {
            if (icqUserNorm.getType() == RosterPacket.ItemType.both) {
                log.info("User succesfully registered");
                System.err.println("User succesfully registered on : " + gatewayJid);
                return true;
            }

            RosterPacket.ItemStatus status = icqUserNorm.getStatus();
            log.info("     User is not subscribed yet. Status : " + status);
//                Presence presence = roster.getPresence(userJid);
//                if (presence.getMode() != Presence.Mode.)

            Presence presencePacket = new Presence(Presence.Type.subscribe);
            presencePacket.setTo(userJid);
            presencePacket.setFrom(conn.getUser());
            conn.sendPacket(presencePacket);
        }
        return false;
    }

    private void showRosterInfo(Roster roster) {
/*
        log.info("Roster unified entries...");
        for (RosterEntry rosterEntry : roster.getUnfiledEntries()) {
            log.info("    Roster unified entry : " + rosterEntry.getClass());
            log.info("        Type : " + rosterEntry.getType());
            log.info("        Status : " + rosterEntry.getStatus());
            log.info("        User : " + rosterEntry.getUser());
            log.info("        Name : " + rosterEntry.getName());
            log.info("        Groups : " + rosterEntry.getGroups());
            Presence userPresence = roster.getPresence(rosterEntry.getUser());
            log.info("            Presence : " + userPresence);
        }

*/
        log.info("Roster entries...");
        for (RosterEntry rosterEntry : roster.getEntries()) {
            log.info("    Roster entry : " + rosterEntry.getClass());
            log.info("        Type : " + rosterEntry.getType());
            log.info("        Status : " + rosterEntry.getStatus());
            log.info("        User : " + rosterEntry.getUser());
            log.info("        Name : " + rosterEntry.getName());
            log.info("        Groups : " + rosterEntry.getGroups());
            Presence userPresence = roster.getPresence(rosterEntry.getUser());
            log.info("            Presence : " + userPresence);
        }

/*
        List<RosterEntry> rosterEntries = new ArrayList<RosterEntry>(roster.getUnfiledEntries());
        for (RosterEntry rosterEntry : rosterEntries) {
            if (rosterEntry.getUser().contains("icq")) {
                roster.removeEntry(rosterEntry);
            }
        }
*/

/*
        log.info("Roster entries...");
        for (RosterEntry rosterEntry : roster.getEntries()) {
            log.info("    Roster entry : " + rosterEntry);
            Presence userPresence = roster.getPresence(rosterEntry.getUser());
            log.info("        Presence : " + userPresence);
        }
*/

        log.info("Roster groups...");
        for (RosterGroup rosterGroup : roster.getGroups()) {
            log.info("    Roster group : " + rosterGroup);
            log.info("        Name : " + rosterGroup.getName());
            log.info("        Entries : " + rosterGroup.getEntries());
        }
    }

    private void waitPressKey(String message) throws IOException {
        log.info("Press enter to " + message + " ...");
        System.in.read();
        log.info("    ok");
    }


    /**
     * JEP 100 4.1.1 Sequence.  Basic Registration
     * @throws Exception
     */
    private void registerOnGateway(XMPPConnection conn, String gatewayJid) {
        log.info("Register");

        {
            PacketCollector collector = conn.createPacketCollector(new PacketFilter() {
                public boolean accept(Packet arg0) {
                    return arg0 instanceof Registration;
                }});

            // 4.1.1.3 Jabber User sends IQ get qualified by the In-Band Registration
            Registration request = new Registration();
            request.setType(IQ.Type.GET);
            request.setTo(gatewayJid);
            conn.sendPacket(request);

            // 4.1.1.4 Gateway returns IQ result to Jabber User, specifying information that
            //  is required in order to registerOnGateway
            Registration response = (Registration)collector.nextResult();

            log.info("    Gateway responce...");
            log.info("        From : " + response.getFrom());
            log.info("        Attributes : " + response.getAttributes());
            log.info("        Extensions : " + response.getExtensions());
            log.info("        Instructions : " + response.getInstructions());
            log.info("        XML : " + response.getChildElementXML());
            collector.cancel();
        }


        Registration registration = new Registration();
        registration.setType(IQ.Type.SET);
        registration.setTo(gatewayJid);
/*
        registration.setFrom(conn.getUser());
        registration.addExtension(new PacketExtension() {
                   public String getElementName() {
                       return "x";
                   }

                   public String getNamespace() {
//                         return "jabber:iq:gateway:registerOnGateway";
                       return "jabber:iq:registerOnGateway";

                   }

                   public String toXML() {
                       StringBuilder builder = new StringBuilder();
                       builder.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\"/>");
                       return builder.toString();
                   }
               });
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("username", username);
        attributes.put("password", password);
        registration.setAttributes(attributes);
*/

        DataForm df = new DataForm("submit");
        FormField pwdField = new FormField("password");
        pwdField.addValue(ICQ_PASSWORD);
        FormField usrField = new FormField("username");
        usrField.addValue(ICQ_LOGIN);

        df.addField(pwdField);
        df.addField(usrField);
        registration.addExtension(df);


        log.info("    create packetCollector and send message");

        PacketCollector collector = conn.createPacketCollector(new PacketIDFilter(registration.getPacketID()));
        conn.sendPacket(registration);

        log.info("    message sent; waiting");

        IQ response = (IQ) collector.nextResult(10000);
        collector.cancel();

        if (response == null) {
            log.warn("    no response from server for register: " + gatewayJid);
//            gatewayActiveStatus.put(gwPrefix, new Boolean(false));
//            return true;
            return;
        }

        if (response.getType() == IQ.Type.ERROR) {
//            errorMessage = response.getError().getMessage();
            log.warn("error registering with gateway: " + gatewayJid + " error=" + response.getError().toXML());
//            gatewayActiveStatus.put(gwPrefix, new Boolean(false));
//            return true;
            return;
        }

//        gatewayActiveStatus.put(gwPrefix, new Boolean(true));

        log.info("    sending presence to gateway");
        Presence p = new Presence(Presence.Type.available);
        p.setTo(gatewayJid);
        p.setFrom(conn.getUser());
        conn.sendPacket(p);
//        return false;
        log.info("Register ok. Waiting for presence responces...");
    }

    /**
     * JEP 100 4.3 Unregistration
     */
    private void unregisterOnGateway(XMPPConnection conn, final String gatewayJid) {
        log.info("Unsubscribe from " + gatewayJid);
        // 4.3.1.1 Jabber User sends IQ set in 'jabber:iq:registerOnGateway' namespace to Gateway,
        //  containing empty <remove/> element
        Registration conv1 = new Registration();
        conv1.setType(IQ.Type.SET);
        Map<String, String> attr = new HashMap<String, String>();
        attr.put("remove", null);
        conv1.setAttributes(attr);
        conv1.setFrom(conn.getUser());
//        conv1.setFrom(JABBER_LOGIN);
        conv1.setTo(gatewayJid);

        PacketCollector resultCollector = conn.createPacketCollector(
                new PacketIDFilter(conv1.getPacketID()));
        PacketCollector presenceCollector = conn.createPacketCollector(
                new PacketTypeFilter(Presence.class));

        conn.sendPacket(conv1);

/*
//        <iq id="fqzfF-10" to="icq.freeside.ru" type="get"><query xmlns="jabber:iq:registerOnGateway"><remove>null</remove></query></iq>
        conn.sendPacket(new Packet() {
            @Override
            public String toXML() {
                return "<iq id='fqzfF-10' to='" + gatewayJid + "' type='set'><query xmlns='jabber:iq:registerOnGateway'><remove/></query></iq>";
            }
        });
*/

        log.info("Unsubscribed. Waiting for unsubscribe presence packets...");
        // 4.3.1.2 and 4.3.1.3 happen on the server

        // 4.3.1.4 Gateway sends IQ result to Jabber User
        Packet response = resultCollector.nextResult();
        log.info("    Info from unregister : " + response);
        if (response != null) {
            log.info("        " + response.getClass());
        }
    }

    /*
     * JEP-100 4.4.1 Log Out
     */
    private void loginToGateway(XMPPConnection conn, String gatewayJid) {
        Presence p = new Presence(Presence.Type.available);
        p.setTo(gatewayJid);
        conn.sendPacket(p);
    }

    /*
     * JEP-100 4.5.1 Log Out
     */
    private void logoutFromGateway(XMPPConnection conn, String gatewayJid) throws Exception {
        log.info("Logout from " + gatewayJid);
        // 4.5.1 Jabber User sends unavailable presence broadcast to Server
        Presence unavailable = new Presence(Presence.Type.unavailable);
        unavailable.setTo(gatewayJid);
        PacketCollector collector = conn.createPacketCollector(new PacketTypeFilter(Presence.class));
        conn.sendPacket(unavailable);
        boolean loggedOut = false;
        while(!loggedOut) {
            Presence p = (Presence) collector.nextResult(5 * 1000);
            if(p == null) {
                log.info("Unable to get UNAVAILABLE packet...this may be due to JM not publishing the response");
                break;
            }
            if(p.getType().equals(Presence.Type.unavailable)) {
                loggedOut = true;
            }
        }
        log.info("Logout OK");
    }


    /*
     * JEP-100 4.6.1 Add a contact
     */
    private void JEP100_461_addContact(XMPPConnection conn, String gatewayJid, String icqUserID) throws Exception {
        log.info("Add ICQ contact " + icqUserID);
        Roster roster = conn.getRoster();

        String absoluteFriend = icqUserID + "@" + gatewayJid;

        // add entry
        roster.createEntry(absoluteFriend, icqUserID, new String[] {"test-icq"});

/*
        Thread.sleep(5 * 1000); // allow for some time to pass
        // inspect results
        RosterEntry entry = roster.getEntry(absoluteFriend);
        assert entry != null;
        assert entry.getType() == RosterPacket.ItemType.both;
*/

        log.info("Add ICQ contact OK");
    }

    /**
     * JEP 100 4.7.1 Delete Contact
     *
     * Jabber User sends IQ set qualified by the 'jabber:iq:roster' namespace, containing subscription
     * attribute with value of "remove".
     * @throws Exception
     */
//    @Test(groups={"cleanup"},dependsOnMethods={"JEP100_461_addContact"})
    private void JEP100_471_deleteContact(XMPPConnection conn, String gatewayJid, String icqUserID) throws Exception {
        Roster roster = conn.getRoster();
        String absoluteFriend = icqUserID + "@" + gatewayJid;

        RosterEntry entry = roster.getEntry(absoluteFriend);
        roster.removeEntry(entry);

/*
        Thread.sleep(5 * 1000);
        assert roster.getEntry(absoluteFriend) == null;
*/
    }

    /**
     * JEP 100 4.8.1 Naturally, the Jabber User may want to exchange messages
     * with a Legacy User.
     * @throws Exception
     */
//    @Test(groups={"nominal"}, dependsOnMethods={"JEP100_511_legacyAddContact"},
//            dependsOnGroups={"setup.*"})
    private void JEP100_481_sendMessage(XMPPConnection conn, String gatewayJid, String icqUserID) throws Exception {
        Chat chat = conn.getChatManager().createChat(icqUserID + '@' + gatewayJid, new SimpleMessageListener());
        chat.sendMessage("Neither, fair saint, if either thee dislike.");
        chat.sendMessage("Русское сообщение");
        chat.sendMessage("Mixed сообщение");
/*
        Message message = chat.nextMessage(5 * 1000);
        assert message != null;
        assert message.getBody() != null;
        assert message.getBody().equalsIgnoreCase("Neither, fair saint, if either thee dislike.");
*/
    }




    private void discoverIdentities(String entityID, String node) throws XMPPException, NoSuchFieldException, IllegalAccessException {
        log.info("Discover identities on " + entityID + " : " + node + "...");
        DiscoverInfo discoverInfo = discoManager.discoverInfo(entityID);

        log.info("    Node : " + discoverInfo.getNode());
        // Get the discovered items of the queried XMPP entity
        for (Iterator<DiscoverInfo.Identity> it = discoverInfo.getIdentities(); it.hasNext();) {
            DiscoverInfo.Identity item = it.next();
            log.info("        Identity : " + item);
            log.info("            Category : " + item.getCategory());
            log.info("            Name : " + item.getName());
            log.info("            Type : " + item.getType());
            log.info("            XML : " + item.toXML());
        }
        List<DiscoverInfo.Feature> features = (List<DiscoverInfo.Feature>) PrivilegedAccessor.getValue(discoverInfo, "features");
        for (DiscoverInfo.Feature feature : features) {
            log.info("        Feature : " + feature);
            log.info("            Var : " + feature.getVar());
        }
        log.info("Discover identities OK");
    }

    private void discoverItems(String entityID, String node) throws XMPPException {
        log.info("Discover services on " + entityID + " : " + node + "...");
        // Get the items of a given XMPP entity
        // This example gets the items associated with online catalog service
        DiscoverItems discoItems = discoManager.discoverItems(entityID, node);

        // Get the discovered items of the queried XMPP entity
        for (Iterator<DiscoverItems.Item> it = discoItems.getItems(); it.hasNext();) {
            DiscoverItems.Item item = it.next();
            log.info("        Item : " + item);
            log.info("            EntityID : " + item.getEntityID());
            log.info("            Node : " + item.getNode());
            log.info("            Name : " + item.getName());
            log.info("            Action : " + item.getAction());
            log.info("            XML : " + item.toXML());
        }
        log.info("Discover services OK");
    }

    public static void main(String[] args) throws Exception {
        TestType testType = TestType.valueOf(args[0]);
        new JabberTest().doTestJabber(testType);
    }


    public class SimpleMessageListener implements MessageListener {
            public void processMessage(Chat chat, Message message) {
                log.info("    Received message: ");
                log.info("        subj: " + message.getSubject());
                log.info("        from: " + message.getFrom());
                log.info("        to: " + message.getTo());
                log.info("        type: " + message.getType());
                log.info("        thread: " + message.getThread());
                log.info("        body: " + message.getBody());
            }
        }
}
