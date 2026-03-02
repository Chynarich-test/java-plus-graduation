package ru.yandex.practicum.user.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.user.action.UserActionProto;
import ru.yandex.practicum.grpc.user.service.UserActionControllerGrpc;
import ru.yandex.practicum.user.mapper.UserActionProtoMapper;
import ru.yandex.practicum.user.model.UserAction;
import ru.yandex.practicum.user.service.UserService;

@GrpcService
@RequiredArgsConstructor
public class UserController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionProtoMapper userActionProtoMapper;
    private final UserService userService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            UserAction userAction = userActionProtoMapper.toModel(request);

            userService.collectUserAction(userAction);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e){
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
