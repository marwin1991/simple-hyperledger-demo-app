package pl.edu.agh.zmilczak.simplehyperledgerdemoapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static pl.edu.agh.zmilczak.simplehyperledgerdemoapp.AuthorizationService.APP_USER;
import static pl.edu.agh.zmilczak.simplehyperledgerdemoapp.AuthorizationService.NETWORK_CONFIG_PATH;

@Service
public class LedgerServices {

    private Gateway.Builder builder;
    private Wallet wallet;
    private Path networkConfigPath;

    @Autowired
    public LedgerServices(AuthorizationService authorizationService) throws IOException {
        wallet = authorizationService.getWallet();
        networkConfigPath = Paths.get(NETWORK_CONFIG_PATH);
        builder = Gateway.createBuilder();
    }

    public List<Result> getAllCars() throws ContractException, IOException {
        builder.identity(wallet, APP_USER).networkConfig(networkConfigPath).discovery(true);
        try (Gateway gateway = builder.connect()) {

            byte[] result = getContract(gateway).evaluateTransaction("queryAllCars");

            ObjectMapper mapper = new ObjectMapper();
            TypeReference<List<Result>> typeRef = new TypeReference<List<Result>>() {};
            return mapper.readValue(new String(result), typeRef);
        }
    }


    public String createCar(Result input) throws ContractException, IOException, TimeoutException, InterruptedException {
        builder.identity(wallet, APP_USER).networkConfig(networkConfigPath).discovery(true);
        try (Gateway gateway = builder.connect()) {

            byte[] result = getContract(gateway).submitTransaction("createCar",
                    input.getKey(),
                    input.getRecord().getMake(),
                    input.getRecord().getModel(),
                    input.getRecord().getColor(),
                    input.getRecord().getOwner());

            return new String(result);
        }
    }

    private Contract getContract(Gateway gateway){
        Network network = gateway.getNetwork("mychannel");
        return network.getContract("fabcar");
    }
}
