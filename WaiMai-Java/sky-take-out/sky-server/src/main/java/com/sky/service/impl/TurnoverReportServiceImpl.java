package com.sky.service.impl;

import com.sky.dto.OrderCountDTO;
import com.sky.dto.TopCountDTO;
import com.sky.dto.UserCountDTO;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrderService;
import com.sky.service.TurnoverReportService;
import com.sky.service.WorkspaceService;
import com.sky.utils.ListToStrUtil;
import com.sky.vo.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TurnoverReportServiceImpl implements TurnoverReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;


//    根据起始时间和结束时间得到营业额
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        dateList.add(beginDate);
        while(!beginDate.isEqual(endDate)){
            beginDate = beginDate.plusDays(1);
            dateList.add(beginDate);
        }
        for(LocalDate date : dateList){
            Double turnover=orderMapper.getTurnoverStatistics(date,date);
            if(turnover==null){
                turnover=0.0;
            }
            turnoverList.add(turnover);
        }
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(ListToStrUtil.join(dateList));
        turnoverReportVO.setTurnoverList(ListToStrUtil.join(turnoverList));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Long> totalUserList = new ArrayList<>();//总用户量
        List<Long> newUserList = new ArrayList<>();//新增用户量
        dateList.add(beginDate);
        while(!beginDate.isEqual(endDate)){
            beginDate = beginDate.plusDays(1);
            dateList.add(beginDate);
        }
        for(LocalDate date : dateList){
            UserCountDTO turnover=userMapper.getUserList(date,date);
            totalUserList.add(turnover.getDayTotalUser());
            newUserList.add(turnover.getDayNewUser());
        }
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(ListToStrUtil.join(dateList));
        userReportVO.setTotalUserList(ListToStrUtil.join(totalUserList));
        userReportVO.setNewUserList(ListToStrUtil.join(newUserList));
        return userReportVO;
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Long> orderCountList = new ArrayList<>();//当天订单数
        List<Long> validOrderCountList = new ArrayList<>();//当天有效订单数
        Long totalOrderCount = 0L;
        Long validOrderCount = 0L;

        dateList.add(beginDate);
        while(!beginDate.isEqual(endDate)){
            beginDate = beginDate.plusDays(1);
            dateList.add(beginDate);
        }
        for(LocalDate date : dateList){
            OrderCountDTO turnover=orderMapper.getOrderList(date,date);
            orderCountList.add(turnover.getValidOrderCount());
            validOrderCountList.add(turnover.getValidOrderCount());
            totalOrderCount+=turnover.getOrderCount();
            validOrderCount+=turnover.getValidOrderCount();
        }
        OrderReportVO orderReportVO = new OrderReportVO();
        orderReportVO.setDateList(ListToStrUtil.join(dateList));
        orderReportVO.setOrderCountList(ListToStrUtil.join(orderCountList));
        orderReportVO.setValidOrderCountList(ListToStrUtil.join(validOrderCountList));
        orderReportVO.setTotalOrderCount(totalOrderCount);
        orderReportVO.setValidOrderCount(validOrderCount);
        Double orderCompletionRate =totalOrderCount ==0?0:(double) validOrderCount / totalOrderCount;
        orderReportVO.setOrderCompletionRate(orderCompletionRate);
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();
        List<String> nameList = new ArrayList<>();//商品名称
        List<Long> numberList = new ArrayList<>();//商品销售量
        dateList.add(beginDate);
        List<TopCountDTO> turnover = orderMapper.getTop10(beginDate,endDate);
        for(TopCountDTO t : turnover){
            nameList.add(t.getName());
            numberList.add(t.getSaleCount());
        }
        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        salesTop10ReportVO.setNameList(ListToStrUtil.join(nameList));
        salesTop10ReportVO.setNumberList(ListToStrUtil.join(numberList));
        if(salesTop10ReportVO.getNameList()==""||salesTop10ReportVO.getNumberList()=="")
            return null;
        return salesTop10ReportVO;
    }
    @Override
    public void export(HttpServletResponse response) throws IOException {
//        根据已有模板创建对象
        LocalDate end = LocalDate.now();
        LocalDate begin = end.minusDays(29);
        InputStream inputStream = this.getClass().getResourceAsStream("/template/运营数据报表模板.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        String timeStr=begin + "-" + end;
        sheet.getRow(1).getCell(2).setCellValue(timeStr);
//        读取数据并写入文件
        BusinessDataVO overviewData=workspaceService.businessData(begin,end);
        //顶部数据
        sheet.getRow(3).getCell(2).setCellValue(overviewData.getTurnover());
        sheet.getRow(3).getCell(4).setCellValue(overviewData.getOrderCompletionRate());
        sheet.getRow(3).getCell(6).setCellValue(overviewData.getNewUsers());
        sheet.getRow(4).getCell(2).setCellValue(overviewData.getValidOrderCount());
        sheet.getRow(4).getCell(4).setCellValue(overviewData.getUnitPrice());
//        9.1,9.2,9.3
        int i=0;
        String fileName = "近30天运营数据报表_" + begin + "_" + end + ".xlsx";
        while(!begin.isAfter(end)){
            BusinessDataVO data=workspaceService.businessData(begin,begin);
            sheet.getRow(7+i).getCell(1).setCellValue(String.valueOf(begin));//日期
            sheet.getRow(7+i).getCell(2).setCellValue(data.getTurnover());//营业额
            sheet.getRow(7+i).getCell(3).setCellValue(data.getValidOrderCount());//有效
            sheet.getRow(7+i).getCell(4).setCellValue(data.getOrderCompletionRate());//完成率
            sheet.getRow(7+i).getCell(5).setCellValue(data.getUnitPrice());//平均
            sheet.getRow(7+i).getCell(6).setCellValue(data.getNewUsers());//新增
            begin=begin.plusDays(1);
            i++;
        }
//        设置响应头，浏览器识别为文件下载
        response.setContentType("application/octet-stream");
        String encodeFileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodeFileName);

        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
//        关闭资源
        outputStream.close();
        workbook.close();
        inputStream.close();
    }
}
