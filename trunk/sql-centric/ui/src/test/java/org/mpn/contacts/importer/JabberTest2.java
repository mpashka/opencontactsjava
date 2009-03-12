package org.mpn.contacts.importer;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Created by IntelliJ IDEA.
 * User: moukhataevs
 * Date: 26.12.2008
 * Time: 12:31:00
 * To change this template use File | Settings | File Templates.
 */
public class JabberTest2 {

    static final Logger log = Logger.getLogger(JabberTest2.class);

    enum ServerType {
        unavailable, nogateway, noregistration, noresponce, registrationerror, notsubscribed, notvisibleme, notvisiblehim, messageMirror, sameMessage
    }

    private static final boolean BREAK_IF_ERROR = false;

    private static final String JABBER_SERVER = "talk.google.com";
    private static final int JABBER_PORT = 5222;
    private static final String JABBER_SERVICE = "gmail.com";
    private static final boolean JABBER_SASL_AUTHENTICATION_ENABLED = true;

    private static final TestProperties TEST_PROPERTIES = TestProperties.getInstance();

    private static final String JABBER_CLIENT = "javatest";
    private static final String JABBER_LOGIN = TEST_PROPERTIES.getGoogleLogin() + "@gmail.com";
    private static final String JABBER_PASSWORD = TEST_PROPERTIES.getGooglePassword();


    private static final String[] ICQ_GATEWAYS = {
// Source : http://bombus.jrudevels.org/wiki/howto/howto_icq
            "chaoslab.info", 	 "13.net.ru", 	 "deac.ru",
            "geeklife.ru", 	"aftar.ru", 	"jabber.krastalk.ru",
            "gelf.no-ip.org", 	"blasux.ru",
            "informjust.ua", 	"calypso.cn.ua",
            "jabbe.net.ru", 	"deac.ru",
            "jabber.corbina.ru", 	"freeside.ru",
            "jabber.cv.ua", 	"gornyak.net",
            "jabber.crimea.ua", 	"highsecure.ru",
            "jabber.krasu.ru", 	"intramail.ru",
            "jabber.org.ru", 	"jabber.chirt.ru",
            "jabber.spbu.ru", 	"jabber.ck.ua",
            "jabber.splc.ru", 	"jabber.kiev.ua",
            "jabber.te.ua", 	"jabber.lviv.ua",
            "jabber.ukrwest.net", 	"jabber.nnov.net",
            "plotinka.ru", 	"jabber.rfei.ru",
            "sgtp.samara.ru", 	"jabber.rikt.ru",
            "tr.element.dn.ua", 	"jabber.stniva.ru",
            "jabber.te.ua", 	"mytlt.ru",
            "jabber.tsure.ru", 	"jabber.uch.net",
            "13.net.ru", 	"jabe.ru",
            "jabber.b.gz.ru", 	"kanet.ru",
            "jabber.fds-net.ru", 	"mytlt.ru",
            "jabber.krastalk.ru", 	"nclug.ru",
            "jmsk.legion.ru", 	"ratelcom.ru",
            "jabber.kursk.lug.ru", 	"smim.ru",
            "vlg.lukoil.ru", 	"udaff.com",
            "myid.ru", 	"jabber.sv.mh.ru",
            "ilikejabber.ru", 	"medexport-omsk.ru",
            "jabber.nwg-nv.ru", 	"jabe.ru",
            "mo.pp.ru", 	"office.ksn.ru",
            "proc.ru",
            "ru","lezz.ru",


// Source : http://www.ejabberd.im/servers
            "2on.net",
            "aszlig.net",
            "autistici.org",
            "AwaitsYou.com",
            "chat.gizmoproject.com",
            "crocobox.org",
            "debianforum.de",
            "draugr.de",
            "erlang-projects.org",
            "fab4.be",
            "gmx.de",
            "goim.us",
            "hapi.pl",
            "jabber.3gnt.org",
            "jabster.pl",
            "jabber.belnet.be",
            "jabber.cz",
            "jabber.dol.ru",
            "jabber.dn.ua",
            "jabber-hispano.org",
            "jabber.i-pobox.net",
            "jabber.i1.ru",
            "jabber.ivanovo.ru",
            "jabber.kiev.ua",
            "jabber.linux.it",
            "jabber.linuxlovers.at",
            "jabber.lotsofshells.net",
            "jabber.minus273.org",
            "jabber.no",
            "jabber.org",
            "jabber.org.au",
            "jabber.postel.org",
            "jabber.psg.com",
            "jabber.rtelekom.ru",
            "jabber.ru",
            "jabber.se",
            "jabber.sbin.org",
            "jabber.snc.ru",
            "jabber.ttn.ru",
            "jabber.ulyssis.org",
            "jabber.xs4all.nl",
            "jabber4friends.de",
            "jabberquebec.net",
            "jabberchicago.net",
            "jabberes.org",
            "jabberland.com",
            "jabberme.de",
            "jabbernet.es",
            "jabbim.cz",
            "jabe.ru",
            "jaim.at",
            "jbother.org",
            "kdetalk.net",
            "njs.netlab.cz",
            "shady.nl",
            "phcn.de",
            "ugatu.net",
            "unstable.nl",
            "ursine.ca",
            "volgograd.ru",
            "volity.net",
            "xmpp.ru",

    };

    private static String ICQ_ACCOUNTS[][];
//    private static final String ICQ_ACCOUNTS[][] = {
////            {"166679359", "111"},       // Main account
//            {"166684632", "111"},
//            {"388548327", "fB2RNaj2"},      // Old father
//            {"224855187", "58001105"},
//            {"199220486", "58001105"},
//            {"117438954", "58001105"},
//            {"316680527", "oooooo"},        // Old marka
//};

    private static String[] CHAT_MESSAGES = {
            "Neither, fair saint, if either thee dislike.",
            "Русское сообщение",
            "Mixed сообщение",
            "Hello ICQ"
    };

    private static final String ICQ_USER = "166679359";

    private static final long OPERATION_TIMEOUT = 10000;

    private ServiceDiscoveryManager discoManager;
    private XMPPConnection conn;

    private int icqUserNumber;
    private String gatewayJid;

    private SimpleMessageListener simpleMessageListener = new SimpleMessageListener();

    private Map<String, Set<ServerType>> testResults = new TreeMap<String, Set<ServerType>>();
    private Set<String> aliveServers = new HashSet<String>();

    Map<String, Presence> presenceData = new ConcurrentHashMap<String, Presence>();

    private boolean presenceExists;

    @Test
    public void testJabber() throws IOException {
        try {
            doTestJabber();
        } catch (Exception e) {
            log.error("Jabber Error", e);
        }
    }


    public void doTestJabber() throws Exception {
        String[] icqAccountsStrings = TEST_PROPERTIES.getProperty("icq.users").split(";");
        ICQ_ACCOUNTS = new String[icqAccountsStrings.length][];
        for (int i = 0; i < icqAccountsStrings.length; i++) {
            String icqAccountsString = icqAccountsStrings[i];
            String[] icqAccountStrings = icqAccountsString.split(":", 2);
            ICQ_ACCOUNTS[i] = icqAccountStrings;
        }

// Create a connection to the jabber.org server on a specific port.
        ConnectionConfiguration config = new ConnectionConfiguration(JABBER_SERVER, JABBER_PORT, JABBER_SERVICE);
        config.setSASLAuthenticationEnabled(JABBER_SASL_AUTHENTICATION_ENABLED);

        conn = new XMPPConnection(config);

        conn.connect();

        conn.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
//                log.trace("                >>> Incoming packet : " + packet.getClass() + " -> " + packet.toXML());
                if (packet instanceof Presence) {
                    presenceExists = true;
                    Presence presence = (Presence) packet;
                    if (presence.getType() == Presence.Type.subscribe) {
                        // Accept all subscription requests.
                        Presence response = new Presence(Presence.Type.subscribed);
                        response.setTo(presence.getFrom());
                        conn.sendPacket(response);
                        log.info("            Subscribed for " + presence.getFrom());
                        if (!presence.getFrom().endsWith(gatewayJid)) {
                            log.warn("Unknown subscription : " + presence.getFrom());
                        }
                    } else if (presence.getType() == Presence.Type.unsubscribe) {
                        // Acknowledge and accept unsubscription notification so that the
                        // server will stop sending notifications saying that the contact
                        // has unsubscribed to our presence.
                        Presence response = new Presence(Presence.Type.unsubscribed);
                        response.setFrom(conn.getUser());
                        response.setTo(presence.getFrom());
                        conn.sendPacket(response);
                        // Otherwise, in manual mode so ignore.
                        log.info("            UnSubscribed for " + presence.getFrom());
                    } else {
                        log.trace("            Presence [" + presence.getFrom() + "] : " + presence.toXML());
                        presenceData.put(presence.getFrom().replaceAll("/.*", ""), presence);
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
//                log.trace("                <<< Outgoing packet : " + packet.toXML());
            }
        }, null);

        conn.login(JABBER_LOGIN, JABBER_PASSWORD, JABBER_CLIENT);

        Roster roster = conn.getRoster();
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);

        {
// Create a new presence. Pass in false to indicate we're unavailable.
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("Hello! I am here! Testing ICQ Gateways...");
// Send the packet (assume we have a XMPPConnection instance called "con").
            conn.sendPacket(presence);
        }

        // Obtain the ServiceDiscoveryManager associated with my XMPPConnection
        discoManager = ServiceDiscoveryManager.getInstanceFor(conn);

        simpleMessageListener.setDaemon(true);
        simpleMessageListener.start();

        for (String jabberGateway : ICQ_GATEWAYS) {
            try {
                if (testJabberGateway(jabberGateway) == 0) {
//                    log.info("    No ICQ gateways");
                    testResults.put(jabberGateway, EnumSet.of(ServerType.nogateway));
                }
            } catch (Exception e) {
//                log.error("IO Error testing " + jabberGateway + " : " + e);
                testResults.put(jabberGateway, EnumSet.of(ServerType.unavailable));
            }
        }

//        waitPressKey("disconnect");

        conn.disconnect();
//        conn.disconnect(new Presence(Presence.Type.unavailable, "I am away. Я ушел...", 0, Presence.Mode.away));

//        waitPressKey("EXIT");

        log.info("Test results...");
        for (Map.Entry<String, Set<ServerType>> stringSetEntry : testResults.entrySet()) {
            log.info("Server : " + stringSetEntry.getKey() + ", result : " + stringSetEntry.getValue());
        }

        log.info("Alive servers...");
        for (String aliveServer : aliveServers) {
            log.info(aliveServer);
        }
    }

    /*
     *
     */
    private int testJabberGateway(String jabberGateway) throws Exception {
        // Get the items of a given XMPP entity
        // This example gets the items associated with online catalog service
        DiscoverItems discoItems = discoManager.discoverItems(jabberGateway);

        int icqGatewayCount = 0;

        // Get the discovered items of the queried XMPP entity
        for (Iterator<DiscoverItems.Item> it = discoItems.getItems(); it.hasNext();) {
            DiscoverItems.Item item = it.next();
            String entityID = item.getEntityID();
//            log.trace("        Entity " + entityID);
            if (!entityID.contains("icq")) continue;
//            log.trace("    Entity info " + entityID);
            DiscoverInfo itemInfo = discoManager.discoverInfo(entityID);
            Iterator<DiscoverInfo.Identity> identityIterator = itemInfo.getIdentities();
            boolean icqGateway = false;
            while (identityIterator.hasNext()) {
                DiscoverInfo.Identity identity =  identityIterator.next();
                if (identity.getCategory().equals("gateway") && identity.getType().equals("icq")) {
                    icqGateway = true;
                }
            }
            if (!itemInfo.containsFeature("jabber:iq:register")) {
//                log.info("        Gateway doesn't support registration : " + entityID);
                testResults.put(entityID, EnumSet.of(ServerType.noregistration));
                continue;
            }
            if (icqGateway) {
//                log.trace("        Check ICQ gateway : " + entityID);
//                List<DiscoverInfo.Feature> features = (List<DiscoverInfo.Feature>) PrivilegedAccessor.getValue(itemInfo, "features");
//                List<DiscoverInfo.Identity> identities = (List<DiscoverInfo.Identity>) PrivilegedAccessor.getValue(itemInfo, "identities");
//                for (DiscoverInfo.Feature feature : features) {
//                    log.trace("            Feature : " + feature.getVar() /*+ "(" + feature.toXML() + ")"*/);
//                }
//                for (DiscoverInfo.Identity identity : identities) {
//                    log.trace("            Identity. Category : " + identity.getCategory() + ", name : " + identity.getName() + ", type : " + identity.getType()/* + " (" + identity.toXML() + ")"*/);
//                }
                icqGatewayCount++;
                EnumSet<ServerType> result = EnumSet.noneOf(ServerType.class);
                gatewayJid = entityID;
                testIcqGateway(result);
                gatewayJid = null;
                testResults.put(entityID, result);
            }
        }
        return icqGatewayCount;
    }

    private void testIcqGateway(EnumSet<ServerType> result) throws Exception {
        Thread.sleep(60 * 1000);
        presenceExists = false;

        log.info(gatewayJid);
        Roster roster = conn.getRoster();
        if (roster.getEntry(gatewayJid) == null) {
            ServerType responseType = registerOnGateway();
            if (responseType != null) {
                result.add(responseType);
            }
        } else {
            log.warn("    Gateway already presents : " + gatewayJid);
            Presence gatewayPresence = roster.getPresence(gatewayJid);
            if (!gatewayPresence.isAvailable()) {
                loginToGateway();
            }
        }
        if (!result.isEmpty()) return;

        String userJid = ICQ_USER + '@' + gatewayJid;
        addContact(userJid);
        Chat chat = conn.getChatManager().createChat(userJid, simpleMessageListener);
        simpleMessageListener.setChat(chat);
//        boolean wasOnline = waitPressKey("check if user was online in ICQ");
        Thread.sleep(60 * 1000);
        if (presenceExists) {
            aliveServers.add(gatewayJid);
        }

        boolean wasOnline = true;
        if (simpleMessageListener.error != null) result.add(simpleMessageListener.error);
        if (simpleMessageListener.error == ServerType.sameMessage) {
            log.warn("    Server message : " + simpleMessageListener.lastMessage);
        }
        simpleMessageListener.setChat(null);
        RosterEntry icqUser = roster.getEntry(userJid);
        boolean icqUserSubscribed = icqUser != null && icqUser.getType() == RosterPacket.ItemType.both;
        Presence icqUserOnlinePresence = presenceData.get(userJid);
        boolean icqUserOnline = icqUserOnlinePresence != null && icqUserOnlinePresence.isAvailable();
        if (!icqUserOnline) result.add(ServerType.notsubscribed);


        if (!wasOnline) result.add(ServerType.notvisibleme);
        if (!icqUserSubscribed) result.add(ServerType.notvisiblehim);

        RosterEntry icqGatewayUser = roster.getEntry(gatewayJid);
        boolean gatewaySubscribed = icqGatewayUser != null && icqGatewayUser.getStatus() != RosterPacket.ItemStatus.SUBSCRIPTION_PENDING;

//        if (!icqUserSubscribed || !icqUserOnline) {
//            log.warn("Use");
//        }

//        logoutFromGateway(gatewayJid);
        unregisterOnGateway();

        removeAllIcqUsers();

    }

    private void removeAllIcqUsers() throws XMPPException {
//        log.info("Remove all icq users...");
        Roster roster = conn.getRoster();
        for (RosterEntry rosterEntry : roster.getEntries()) {
            if (rosterEntry.getUser().endsWith(gatewayJid)) {
                log.trace("    Removing ICQ user " + rosterEntry.getName());
                roster.removeEntry(rosterEntry);
            }
        }
//        log.info("Remove OK");
    }


    private void addContact(String userJid) throws Exception {
        Roster roster = conn.getRoster();
        RosterEntry icqUserNorm = roster.getEntry(userJid);
        if (icqUserNorm == null) {
            roster.createEntry(userJid, userJid, new String[] {"test-icq"});
        }

        icqUserNorm = roster.getEntry(userJid);
        if (icqUserNorm != null && icqUserNorm.getType() == RosterPacket.ItemType.both) {
            log.trace("    User succesfully registered");
        } else {

//            RosterPacket.ItemStatus status = icqUserNorm.getStatus();
//            log.trace("     User is not subscribed yet. Status : " + status);

/*
            {
                Presence presencePacket = new Presence(Presence.Type.subscribe);
                presencePacket.setTo(userJid);
                presencePacket.setFrom(conn.getUser());
                conn.sendPacket(presencePacket);
            }

            {
                Presence presencePacket = new Presence(Presence.Type.subscribed);
                presencePacket.setTo(userJid);
                presencePacket.setFrom(conn.getUser());
                conn.sendPacket(presencePacket);
            }
*/
        }
    }



    /*
     * JEP 100 4.1.1 Sequence.  Basic Registration
     */
    private ServerType registerOnGateway() {
//        log.trace("    Register");

/*
This code is used to get registration form information
*/
        PacketCollector registrationCollector = conn.createPacketCollector(new PacketTypeFilter(Registration.class));
        PacketCollector subscription = conn.createPacketCollector(new PacketTypeFilter(Presence.class));

        try {
            // 4.1.1.3 Jabber User sends IQ get qualified by the In-Band Registration
            Registration request = new Registration();
            request.setType(IQ.Type.GET);
            request.setTo(gatewayJid);
            conn.sendPacket(request);

            // 4.1.1.4 Gateway returns IQ result to Jabber User, specifying information that
            //  is required in order to registerOnGateway
            Registration registrationDataResponse = (Registration) registrationCollector.nextResult();
            if (registrationDataResponse == null) {
                log.warn("    No response [get]");
                return ServerType.noresponce;
            }

/*
        log.trace("        Gateway response...");
        log.trace("            From : " + registrationDataResponse.getFrom());
        log.trace("            Attributes : " + registrationDataResponse.getAttributes());
//            log.trace("            Extensions : " + registrationDataResponse.getExtensions());
        log.trace("            Instructions : " + registrationDataResponse.getInstructions());
        log.trace("            XML : " + registrationDataResponse.getChildElementXML());
//            registrationCollector.cancel();
*/
            if (!registrationDataResponse.getFrom().equals(gatewayJid)) {
                log.warn("    Gateway From differs : " + registrationDataResponse.getFrom());
            }


            Registration registration = new Registration();
            registration.setType(IQ.Type.SET);
            registration.setTo(registrationDataResponse.getFrom());

            String[] icqAccount = ICQ_ACCOUNTS[icqUserNumber % ICQ_ACCOUNTS.length];
            DataForm df = new DataForm("submit");
            FormField usrField = new FormField("username");
            usrField.addValue(icqAccount[0]);
            FormField pwdField = new FormField("password");
            pwdField.addValue(icqAccount[1]);

            log.info("        " + icqAccount[0]);

            df.addField(pwdField);
            df.addField(usrField);
            registration.addExtension(df);
            icqUserNumber++;


//        log.trace("    create packetCollector and send message");

            conn.sendPacket(registration);

//        log.trace("    message sent; waiting");

            IQ registerResponse = (IQ) registrationCollector.nextResult(OPERATION_TIMEOUT);
//            registrationCollector.cancel();

            if (registerResponse == null) {
                log.warn("    no response [register]");
//            gatewayActiveStatus.put(gwPrefix, new Boolean(false));
//            return true;
                return ServerType.noresponce;
            }

            if (registerResponse.getType() == IQ.Type.ERROR) {
//            errorMessage = registrationDataResponse.getError().getMessage();
                log.warn("    error registering with gateway: " + gatewayJid + " error=" + registerResponse.getError().toXML());
//            gatewayActiveStatus.put(gwPrefix, new Boolean(false));
//            return true;

                if (BREAK_IF_ERROR) {
                    return ServerType.registrationerror;
                }
            }

//        gatewayActiveStatus.put(gwPrefix, new Boolean(true));

//        return loginToGateway(gatewayJid);

            // TODO 4.1.1.8 Optionally, Jabber User sends IQ set qualified by the 'jabber:iq:roster'
            //  namespace to its server


            // 4.1.1.9 Gateway sends subscription request to Jabber User
            for(int i = 0; i < 3; i++) {
                Presence p = (Presence) subscription.nextResult(OPERATION_TIMEOUT);
                if (p == null) {
                    log.warn("    no gateway presence response");
                    if (BREAK_IF_ERROR) {
                        return ServerType.notsubscribed;
                    } else {
                        break;
                    }
                }
                log.trace("        gateway presence: " + p.toXML());
                if(p.getType().equals(Presence.Type.subscribe)) {
                    // 4.1.1.10 Jabber User's client SHOULD approve the subscription request
                    Presence subscribed = new Presence(Presence.Type.subscribed);
                    subscribed.setTo(p.getFrom());
                    conn.sendPacket(subscribed);
                    break;
                } else {
                    log.warn("    unknown presence : " + p.toXML());
                }
            }


            // 4.1.1.11 Jabber User sends subscription request to Gateway
            Presence gatewaySub = new Presence(Presence.Type.subscribe);
            gatewaySub.setTo(gatewayJid);
            conn.sendPacket(gatewaySub);

            // 4.1.1.12 Gateway sends approves subscription request
            for(int i = 0; i < 3; i++) {
                Presence p = (Presence) subscription.nextResult(OPERATION_TIMEOUT);
                if (p == null) {
                    log.warn("    no presense response from server");
                    if (BREAK_IF_ERROR) {
                        return ServerType.notsubscribed;
                    } else {
                        break;
                    }
                }
                log.trace("        gateway subscribe response : " + p.toXML());
                if(p.getType().equals(Presence.Type.subscribed)) {
                    break;
                } else {
                    log.warn("    unknown presence : " + p.toXML());
                }
            }

//            * 4.4.1 login
            {
                Presence p = new Presence(Presence.Type.available);
                p.setTo(gatewayJid);
                conn.sendPacket(p);

                Presence pResult = (Presence) subscription.nextResult(OPERATION_TIMEOUT);
                if (pResult == null) {
                    log.warn("    Login empty responce");
                } else {
                    log.trace("    Login responce : " + p.toXML());
                }
            }



            return null;
        } finally {
            registrationCollector.cancel();
            subscription.cancel();
        }
    }

    /*
     * JEP 100 4.3 Unregistration
     */
    private void unregisterOnGateway() {
//        log.info("Unsubscribe from " + gatewayJid);
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

        PacketCollector resultCollector = conn.createPacketCollector(new PacketIDFilter(conv1.getPacketID()));
        PacketCollector presenceCollector = conn.createPacketCollector(new PacketTypeFilter(Presence.class));

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


//        log.info("Unsubscribed. Waiting for unsubscribe presence packets...");
        // 4.3.1.2 and 4.3.1.3 happen on the server

        // 4.3.1.4 Gateway sends IQ result to Jabber User
        Packet response = resultCollector.nextResult(OPERATION_TIMEOUT);
//        log.info("    Info from unregister : " + response);
        if (response == null) {
            log.warn("    Unregister timeout");
        } else {
            log.trace("    Unregister response : " + response.toXML());

            boolean unsubscribed = false;
            boolean unsubscribe = false;
            boolean unavailable = false;


            while(!unsubscribe || !unsubscribed) {
                Presence p = (Presence)presenceCollector.nextResult(OPERATION_TIMEOUT);
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
                log.warn("    Unable to get UNAVAILABLE...JM does not forward those type of requests");
            }

            presenceCollector.cancel();
            resultCollector.cancel();
        }
    }

    /*
     * JEP-100 4.4.1 Log In
     */
    private boolean loginToGateway() {
        log.trace("            sending presence to gateway");

        {
            Presence p = new Presence(Presence.Type.subscribed);
            p.setTo(gatewayJid);
            p.setFrom(conn.getUser());
            conn.sendPacket(p);
        }


        {
            Presence p = new Presence(Presence.Type.available);
            p.setTo(gatewayJid);
            p.setFrom(conn.getUser());
            conn.sendPacket(p);
        }
        return true;
    }

    /*
     * JEP-100 4.5.1 Log Out
     */
    private void logoutFromGateway() throws Exception {
        log.trace("    Logout from " + gatewayJid);
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




    public static void main(String[] args) throws Exception {
        new JabberTest2().doTestJabber();
    }


    private static boolean waitPressKey(String message) throws IOException {
        log.info("Press (y/n) enter to " + message + " ...");
        char answer = (char) System.in.read();
        while (System.in.available() > 0) {
            System.in.read();
        }
        boolean yes = answer == 'y' || answer == 'Y';
        log.info(yes ? "    yes" : "    no");
        return yes;
    }

    public class SimpleMessageListener extends Thread implements MessageListener {

        private Chat chat;
        private ServerType error;
        private String lastMessage;

        public void setChat(Chat chat) {
            this.chat = chat;
            error = null;
            lastMessage = null;
        }

        public void processMessage(Chat chat, Message message) {
/*
            log.trace("    Received message: ");
            log.trace("        subj: " + message.getSubject());
            log.trace("        from: " + message.getFrom());
            log.trace("        to: " + message.getTo());
            log.trace("        type: " + message.getType());
            log.trace("        thread: " + message.getThread());
            log.trace("        body: " + message.getBody());
*/
            for (String s : CHAT_MESSAGES) {
                if (message.getBody().equals(s)) {
                    error = ServerType.messageMirror;
                    return;
                }
            }
            if (message.getBody().equals(lastMessage)) {
                error = ServerType.sameMessage;
                return;
            }
            error = null;
            log.trace("    Received msg from " + message.getFrom() + " : " + message.getBody());
            lastMessage = message.getBody();
        }

        @Override
        public void run() {
            int index = 0;
            while (true) {
                try {
                    Thread.sleep(3000);
                    Chat localChat = chat;
                    if (localChat != null) {
                        localChat.sendMessage(CHAT_MESSAGES[index++ % CHAT_MESSAGES.length]);
                    }
                } catch (Exception e) {
                    log.error("Chat error", e);
                }
            }
        }
    }

}