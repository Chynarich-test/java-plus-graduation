package ru.yandex.practicum.client;


import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.UserActionType;
import ru.yandex.practicum.grpc.user.action.ActionTypeProto;
import ru.yandex.practicum.grpc.user.action.UserActionProto;
import ru.yandex.practicum.grpc.user.service.UserActionControllerGrpc;

import java.time.Instant;

@Service
public class CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub stub;

    public void collectUserAction(long userId, long eventId, UserActionType actionType){
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        ActionTypeProto protoActionType = mapActionType(actionType);

        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(protoActionType)
                .setTimestamp(timestamp)
                .build();

        stub.collectUserAction(request);
    }

    private ActionTypeProto mapActionType(UserActionType type) {
        return switch (type) {
            case VIEW -> ActionTypeProto.ACTION_VIEW;
            case REGISTER -> ActionTypeProto.ACTION_REGISTER;
            case LIKE -> ActionTypeProto.ACTION_LIKE;
        };
    }
}
