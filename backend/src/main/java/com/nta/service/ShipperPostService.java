package com.nta.service;

import com.nta.dto.response.ShipperResponse;
import com.nta.entity.Shipper;
import com.nta.entity.ShipperPost;
import com.nta.enums.ErrorCode;
import com.nta.enums.ShipperPostStatus;
import com.nta.exception.AppException;
import com.nta.mapper.ShipperMapper;
import com.nta.repository.ShipperPostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipperPostService {
    private static final Logger log = LoggerFactory.getLogger(ShipperPostService.class);
    private final ShipperPostRepository shipperPostRepository;
    private final AuthenticationService authenticationService;
    private final ShipperMapper shipperMapper;

    public long countByPostId(String postId) {
        return shipperPostRepository.countByPostId(postId);
    }

    public ShipperResponse findWinnerByPostId(String postId) {
        final var shippers = shipperPostRepository.findApprovedShippersByPostAndUser(
                postId,
                authenticationService.getUserDetail().getId(),
                ShipperPostStatus.APPROVAL
        );
        if(shippers.isEmpty()) {
            throw new AppException(ErrorCode.CANNOT_FIND_WINNER);
        }
        return shipperMapper.toShipperResponse(shippers.getFirst());
    }

}
