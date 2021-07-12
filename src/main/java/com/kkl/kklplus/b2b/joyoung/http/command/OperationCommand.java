package com.kkl.kklplus.b2b.joyoung.http.command;

import com.kkl.kklplus.b2b.joyoung.http.request.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class OperationCommand {

    public enum OperationCode {

        ORDERLIST(1001, "工单完工接口", "SVC_KUAIKELI_GETBILLSLIST", OrderListRequestParam.class),
        CONFIRM(1002, "工单接单确认接口", "SVC_KUAIKELI_TORECEIVEBILLS",OrderConfirmedRequestParam.class),
        PLANNED(1003, "工单派单接口", "SVC_KUAIKELI_TOASSIGNBILLSTOENGINEER",OrderPlannedRequestParam.class),
        APPOINTED(1004, "工单预约接口", "SVC_KUAIKELI_TOSUBSCRIBEBILLS", OrderAppointedRequestParam.class),
        VISITED(1005, "工单上门接口", "SVC_KUAIKELI_TOVISITBILLS",OrderVisitedRequestParam.class),
        CANCELLED(1006, "工单取消接口", "SVC_KUAIKELI_TOCANCELBILLS",OrderCancelledRequestParam.class),
        COMPLETED(1007, "工单完工接口", "SVC_KUAIKELI_TOCOMPLETEBILLS",OrderCompletedRequestParam.class),
        SAVEPROCESSLOG(1008, "回传工单处理日志接口", "SVC_TO_SYNCHRONIZATION_LOG",OrderProcesslogRequestParam.class),
        MATERIAL(1009,"配件单接口","SVC_ADD_MATERIAL_ORDER",MaterialRequestParam.class),
        //MATERIALARRIVAL(1010,"配件单到货通知接口","SVC_KUAIKELI_TOMATERIALARRIVALBILLS",MaterialArrivalRequestParam.class),
        MATERIALCLOSE(1011,"配件单关闭通知接口","SVC_FINISH_MATERIAL_ORDER",MaterialCloseRequestParam.class),
        HALT(1012, "工单挂起接口", "SVC_PAUSE_SERVICE", OrderHaltRequestParam.class),
        CONSULTINGORDERPROCESS(1013,"咨询单通知接口","SVC_HANDLE_CONSULTING",ConsultingOrderProcessRequestParam.class);
        public int code;
        public String name;
        public String serviceCode;
        public Class reqBodyClass;

        private OperationCode(int code, String name, String serviceCode, Class reqBodyClass) {
            this.code = code;
            this.name = name;
            this.serviceCode = serviceCode;
            this.reqBodyClass = reqBodyClass;
        }
    }

    @Getter
    @Setter
    private OperationCode opCode;

    @Getter
    @Setter
    private RequestParam reqBody;

    public static OperationCommand newInstance(OperationCode opCode, RequestParam reqBody) {
        OperationCommand command = new OperationCommand();
        command.opCode = opCode;
        command.reqBody = reqBody;
        return command;
    }
}
