package tcc.bes.api_monolito.waitingroom.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.queuemanagement.application.QueueManagementPort;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomPort;

@Component
@ConditionalOnProperty(name = {
        "app.jobs.worker.enabled",
        "app.module.waiting-room.enabled"
}, havingValue = "true", matchIfMissing = true)
public class WaitingRoomWorker {

    private final QueueManagementPort queueManagementPort;
    private final WaitingRoomPort waitingRoomPort;

    public WaitingRoomWorker(
            QueueManagementPort queueManagementPort,
            WaitingRoomPort waitingRoomPort
    ) {
        this.queueManagementPort = queueManagementPort;
        this.waitingRoomPort = waitingRoomPort;
    }

    @Scheduled(fixedDelayString = "${app.jobs.worker.fixed-delay-ms}")
    public void run() {
        queueManagementPort.listOpenQueues()
                .forEach(queue -> waitingRoomPort.processQueue(queue.id()));
    }
}
