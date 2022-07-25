package com.cellwize.developersdk.javaclient;

import com.cellwize.developersdk.ApplicationInstance;
import com.cellwize.developersdk.IApplication;
import com.cellwize.developersdk.SdkContext;
import com.cellwize.developersdk.naasservice.managedobjects.INetworkService;
import com.cellwize.developersdk.naasservice.models.*;
import com.cellwize.developersdk.pgwmodel.WorkOrderStatusDetails;
import com.cellwize.developersdk.pgwmodel.orders.WorkItem;
import com.cellwize.developersdk.pgwmodel.orders.enums.AllowPolicyFlowRulesTypes;
import com.cellwize.developersdk.pgwmodel.orders.enums.WorkItemTypes;
import com.cellwize.developersdk.pgwmodel.orders.enums.WorkOrderMethod;
import com.cellwize.developersdk.pgwmodel.orders.enums.WorkOrderProvMode;
import com.cellwize.developersdk.pgwservice.IProvisioningService;
import com.cellwize.developersdk.pgwservice.models.WorkItemBuilder;
import com.cellwize.developersdk.pgwservice.models.WorkOrder;
import com.cellwize.developersdk.pgwservice.models.WorkOrderBuilder;
import com.cellwize.developersdk.pgwservice.models.WorkOrderStatus;
import com.cellwize.developersdk.utils.Vendor;

import java.util.*;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        SdkContext context = new SdkContext("http://192.168.90.9");
        IApplication app = ApplicationInstance.newInstance(context);

        networkServiceDemo(app);
        provisioningServiceDemo(app);
    }

    private static void networkServiceDemo(IApplication app) {
        INetworkService networkService = app.networkService();

        getMosDemo(networkService);
        traversalDemo(networkService);
    }

    private static void getMosDemo(INetworkService networkService) {
        System.out.println("MOs By Criteria");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("parent_uid", "9980384a-efdd-3838-bb25-606cd5a82feb");

        MoCriteria criteria = new MoCriteriaBuilder()
                .parameters(parameters)
                .build();
        Stream<SdkManagedObject> mos = networkService.getMos(criteria);

        mos.forEach(mo ->
                System.out.println("parentUid: " + mo.getParentUid() + ", vendor: " +
                        mo.getVendor() + ", technology: " + mo.getTechnology() + ", uid: " + mo.getUid() +
                        ", scopeId: " + mo.getScopeId() + ", accessor group: " + Objects.requireNonNull(mo.getAccessor()).getGroupId())
        );
    }

    private static void traversalDemo(INetworkService networkService) {
        System.out.println("Target Mos (Traversal Service)");

        HashSet<UUID> sourceIds = new HashSet<>(Collections.singleton(UUID.fromString("014db2a1-5493-376e-9f7b-62c9f9c1d03c")));
        TraverseCriteria criteria = new TraverseCriteriaBuilder()
                .vendor(Vendor.NOKIA)
                .classes("LNCEL", "LNBTS")
                .sourceIds(sourceIds)
                .build();

        List<Traversal> targetMos = networkService.traverse(criteria);

        targetMos.forEach(traversal ->
                System.out.println("sourceMoUid: " + traversal.getSourceMoUid() + ", vendor: " +
                        traversal.getMo().getVendor() + ", technology: " + traversal.getMo().getTechnology())
        );
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
        String id = workOrderStatus.getId();
        WorkOrderStatusDetails wo = provisioningService.getWorkOrder(id);
        System.out.println("WorkOrder [id]: " + wo.getId() + "| [desc]: " +
                wo.getDescription());
    }

}
