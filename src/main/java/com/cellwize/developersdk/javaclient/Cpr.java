package com.cellwize.developersdk.javaclient;

import com.cellwize.developersdk.AppConfig;
import com.cellwize.developersdk.ApplicationInstance;
import com.cellwize.developersdk.IApplication;
import com.cellwize.developersdk.logger.ILogger;
import com.cellwize.developersdk.naasservice.models.ManagedObject;
import com.cellwize.developersdk.naasservice.models.ParameterListEnumeration;

import java.util.stream.Collectors;

public class Cpr {
    public static void main(String[] args) {
        run();
    }

    public static void run() {
        new Cpr().demo();
    }

    private Cpr() {
    }

    private void demo() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("================ CPR Demo ================");
        System.out.println("==========================================");
        System.out.println();

        final AppConfig config = new AppConfig.Builder()
                .showApiLog(true)
                .naasUrl("http://192.168.90.57:9091")
                .pgwUrl("http://192.168.90.57:9093").build();
        final IApplication app = ApplicationInstance.newInstance(config);
        final String targetClusterName = "TESTCLUSTER";

        app.context(targetClusterName, (context) -> {
            ILogger logger = context.getLogger();
            logger.info("initializing population context for target cluster: " + targetClusterName);

            context.getPopulation().forEach(cell -> {
                logger.info("population cell guid: " + cell.getGuid());

                for (ManagedObject mo : cell.getMos("ANR")) {
                    logger.info("cell.getMos(\"ANR\") -> ANR guid: " + mo.getGuid());
                    ParameterListEnumeration lst = mo.getListEnumerationParameter("anrIdleTimeThresLte");
                    logger.info(String.join(",", lst.getItems()));
                    //context.addRecommendation("UPDATE", mo, "some reason");
                }
            });
        });
    }
}
