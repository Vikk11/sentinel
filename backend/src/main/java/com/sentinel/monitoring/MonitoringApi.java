package com.sentinel.monitoring;

import java.util.List;
import java.util.UUID;

public interface MonitoringApi {

    List<CheckResultView> recentChecks(UUID serviceId, int limit);

}