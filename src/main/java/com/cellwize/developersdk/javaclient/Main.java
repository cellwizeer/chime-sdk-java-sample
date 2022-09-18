package com.cellwize.developersdk.javaclient;

import com.cellwize.developersdk.AppConfig;
import com.cellwize.developersdk.ApplicationInstance;
import com.cellwize.developersdk.IApplication;
import com.cellwize.developersdk.naasservice.INetworkService;
import com.cellwize.developersdk.naasservice.builder.MoCriteriaBuilder;
import com.cellwize.developersdk.naasservice.models.*;
import com.cellwize.developersdk.pgw.model.WorkOrderStatusDetails;
import com.cellwize.developersdk.pgw.model.orders.WorkItem;
import com.cellwize.developersdk.pgw.model.orders.enums.AllowPolicyFlowRulesTypes;
import com.cellwize.developersdk.pgw.model.orders.enums.WorkItemTypes;
import com.cellwize.developersdk.pgw.model.orders.enums.WorkOrderMethod;
import com.cellwize.developersdk.pgw.model.orders.enums.WorkOrderProvMode;
import com.cellwize.developersdk.pgwservice.IProvisioningService;
import com.cellwize.developersdk.pgwservice.builder.WorkItemBuilder;
import com.cellwize.developersdk.pgwservice.builder.WorkOrderBuilder;
import com.cellwize.developersdk.pgwservice.models.WorkOrder;
import com.cellwize.developersdk.pgwservice.models.WorkOrderStatus;
import com.cellwize.developersdk.utils.Vendor;

import java.util.*;
import java.util.stream.Stream;

public class Main {
    //https://kb.cellwize.com/pages/viewpage.action?pageId=184418347
    private final static boolean networkServiceDemo = true;
    private final static boolean provisioningServiceDemo = true;
    private final static boolean runCprDemo = false;

    public static void main(String[] args) {
        AppConfig config = new AppConfig.Builder()
                .naasUrl("http://192.168.90.57:9091")
                .pgwUrl("http://192.168.90.57:9093").build();
        IApplication app = ApplicationInstance.newInstance(config);

        if (networkServiceDemo)
            networkServiceDemo(app);

        if (provisioningServiceDemo)
            provisioningServiceDemo(app);

        if (runCprDemo)
            Cpr.run();
    }

    private static void networkServiceDemo(IApplication app) {
        INetworkService networkService = app.networkService();

        getMosDemo(networkService);
        traversalDemo(networkService);
    }

    private static void getMosDemo(INetworkService networkService) {
        System.out.println("MOs By Criteria");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("parent_uid", "5353ddaa-67b7-3748-856f-39c3a53f4ece");

        MoCriteria criteria = new MoCriteriaBuilder()
                .parameters(parameters)
                .fields("uid,meta_type,vendor,technology,attributes,class,parent_uid")
                .build();

        Stream<ManagedObject> mos = networkService.getMos(criteria);

        mos.forEach(mo -> {
            System.out.println("mo uid: " + mo.getUid());
        });
    }

    private static void traversalDemo(INetworkService networkService) {
        System.out.println("Target Mos (Traversal Service)");

        String targetMoCLass = "LNBTS";
        String anotherMoClass = "LNHOIF";

        List<Traversal> traverses = new ArrayList<>();

        Set<UUID> uids = new HashSet<>();
        uids.add(UUID.fromString("014db2a1-5493-376e-9f7b-62c9f9c1d03c"));
        uids.add(UUID.fromString("b07e5b8c-cfa2-3b22-aae2-1467d4cbd632"));

        TraverseCriteria criteria = new TraverseCriteriaBuilder()
                .vendor(Vendor.NOKIA)
                .classes("LNCEL", targetMoCLass)
                .sourceIds(uids)
                .build();

        List<Traversal> targetMos = networkService.traverse(criteria);

        targetMos.forEach(traversal -> {
            System.out.println("sourceMoUid: " + traversal.getSourceMoUid() + ", vendor: " +
                    traversal.getMo().getVendor() + ", technology: " + traversal.getMo().getTechnology());

            traverses.add(traversal);
        });

        traverses.forEach(traversal -> {
            System.out.println("-------------------------------------------------------");
            System.out.println("----- MoId => " + traversal.getMo().getUid() + " ----");
            System.out.println("-------------------------------------------------------");

            System.out.println("Traversing with $anotherMoClass as target class");
            traversal.getMo().traverse(anotherMoClass);

            System.out.println("Traversing with $targetMoCLass as target class");
            traversal.getMo().traverse(targetMoCLass);

            System.out.println("Getting MO $targetMoCLass as target class");
            traversal.getMo().getMos(targetMoCLass);

            System.out.println("Traversing $anotherMoClass as target class again");
            traversal.getMo().traverse(anotherMoClass);
        });
    }

    private static void provisioningServiceDemo(IApplication app) {
        IProvisioningService provisioningService = app.provisioningService();

        workOrderDemo(provisioningService);
    }

    private static void workOrderDemo(IProvisioningService provisioningService) {
        System.out.println("Create work order");

        WorkItem workItems = new WorkItemBuilder()
                .paramName("cellName")
                .values("Simplify cell xyz")
                .maintenanceFlag(false)
                .moId("ef5ef910-2bc1-3cb0-a2f7-b406d3014153")
                .wiType(WorkItemTypes.CHANGE_SPECIFIC_PARAMETER)
                .build();

        ArrayList<WorkItem> list = new ArrayList<>();
        list.add(workItems);

        WorkOrder workOrder = new WorkOrderBuilder()
                .workItems(list)
                .trackingId("1")
                .priority("1")
                .description("chime-mock mo creation")
                .method(WorkOrderMethod.NON_TRANSACTION)
                .mode(WorkOrderProvMode.OFFLINE_SIM)
                .allowPolicyFlowRules(AllowPolicyFlowRulesTypes.IGNORE)
                .build();

        WorkOrderStatus workOrderStatus = provisioningService.sendWorkOrder(workOrder);

        System.out.println("Send WorkOrder Result [id]: " + workOrderStatus.getId() +
                "| [status]: " + Objects.requireNonNull(workOrderStatus.getStatus()).getStatusName());


        System.out.println("Get work order");
        String id = Objects.requireNonNull(workOrderStatus.getId());
        WorkOrderStatusDetails wo = provisioningService.getWorkOrder(id);
        System.out.println("WorkOrder [id]: " + wo.getId() + "| [desc]: " +
                wo.getDescription());
    }
}
