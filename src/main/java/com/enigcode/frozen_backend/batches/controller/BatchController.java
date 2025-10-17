package com.enigcode.frozen_backend.batches.controller;

import com.enigcode.frozen_backend.batches.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class BatchController {

    final BatchService batchService;
}
