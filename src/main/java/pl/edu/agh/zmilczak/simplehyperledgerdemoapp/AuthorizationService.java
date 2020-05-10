package pl.edu.agh.zmilczak.simplehyperledgerdemoapp;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.Set;

@Service
public class AuthorizationService {

    public final static String NETWORK_CONFIG_PATH = "../../test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml";
    private final static String PEM_FILE_PATH = "../../test-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem";
    private final static String ORG1_CA_URL = "https://localhost:7054";

    public final static String APP_USER = "appUser3";
    public final static String ADMIN_USER = "admin";

    public String enrollAdmin() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, InvalidArgumentException, CertificateException, EnrollmentException, org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException {
        HFCAClient caClient = getCaClient();
        Wallet wallet = getWallet();

        // Check to see if we've already enrolled the admin user.
        boolean adminExists = wallet.get(ADMIN_USER) != null;
        if (adminExists) {
            return "An identity for the admin user \"" + ADMIN_USER + "\" already exists in the wallet";
        }

        // Enroll the admin user, and import the new identity into the wallet.
        Enrollment enrollment = getAdminEnrollment(caClient);
        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put(ADMIN_USER, user);

        return "Successfully enrolled user \"" + ADMIN_USER + "\" and imported it into the wallet";
    }


    public String registerUser() throws Exception {
        HFCAClient caClient = getCaClient();
        Wallet wallet = getWallet();

        // Check to see if we've already enrolled the user.
        boolean userExists = wallet.get(APP_USER) != null;
        if (userExists) {
            return "An identity for the user \"" + APP_USER + "\" already exists in the wallet";
        }

        boolean adminExists = wallet.get(ADMIN_USER) != null;
        if (!adminExists) {
            return "\"" + ADMIN_USER + "\" needs to be enrolled and added to the wallet first";
        }

        Identity adminIdentity = wallet.get(ADMIN_USER);
        User admin = getNewUser(getAdminEnrollment(caClient), adminIdentity.getMspId());

        // Register the user, enroll the user, and import the new identity into the wallet.
        RegistrationRequest registrationRequest = new RegistrationRequest(APP_USER);
        registrationRequest.setAffiliation("org1.department1");
        registrationRequest.setEnrollmentID(APP_USER);
        String enrollmentSecret = caClient.register(registrationRequest, admin);
        Enrollment enrollment = caClient.enroll(APP_USER, enrollmentSecret);
        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put(APP_USER, user);
        return "Successfully enrolled user \"" + APP_USER + "\" and imported it into the wallet";
    }


    //Create a CA client for interacting with the CA.
    private HFCAClient getCaClient() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, CryptoException, InvalidArgumentException {
        Properties props = new Properties();
        props.put("pemFile", PEM_FILE_PATH);
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance(ORG1_CA_URL, props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        return caClient;
    }

    // Create a wallet for managing identities
    public Wallet getWallet() throws IOException {
        return Wallets.newFileSystemWallet(Paths.get("wallet"));
    }

    private Enrollment getAdminEnrollment(HFCAClient caClient) throws EnrollmentException, org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException {
        final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
        enrollmentRequestTLS.addHost("localhost");
        enrollmentRequestTLS.setProfile("tls");
        return caClient.enroll(ADMIN_USER, "adminpw", enrollmentRequestTLS);
    }

    private User getNewUser(Enrollment enrollment, String mspId) {
        return new User() {

            @Override
            public String getName() {
                return ADMIN_USER;
            }

            @Override
            public Set<String> getRoles() {
                return null;
            }

            @Override
            public String getAccount() {
                return null;
            }

            @Override
            public String getAffiliation() {
                return "org1.department1";
            }

            @Override
            public Enrollment getEnrollment() {
                return enrollment;
            }

            @Override
            public String getMspId() {
                return mspId;
            }

        };
    }
}
