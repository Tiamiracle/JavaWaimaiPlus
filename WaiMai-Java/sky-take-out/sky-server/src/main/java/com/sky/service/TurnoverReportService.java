package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

public interface TurnoverReportService {
    TurnoverReportVO turnoverStatistics(LocalDate beginDate, LocalDate endDate);

    UserReportVO userStatistics(LocalDate beginDate, LocalDate endDate);

    OrderReportVO ordersStatistics(LocalDate beginDate, LocalDate endDate);

    SalesTop10ReportVO top10(LocalDate beginDate, LocalDate endDate);

    void export(HttpServletResponse response) throws IOException;
}
