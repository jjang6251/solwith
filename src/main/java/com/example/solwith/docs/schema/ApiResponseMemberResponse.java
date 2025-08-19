package com.example.solwith.docs.schema;

import com.example.solwith.common.ApiResponse;
import com.example.solwith.dto.MemberResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ApiResponseMemberResponse extends ApiResponse<MemberResponse> {
    public ApiResponseMemberResponse(int status, String message, MemberResponse data, List<ErrorDetail> errors, String path, String traceId, LocalDateTime timestamp, Map<String, Object> meta) {
        super(status, message, data, errors, path, traceId, timestamp, meta);
    }
}
