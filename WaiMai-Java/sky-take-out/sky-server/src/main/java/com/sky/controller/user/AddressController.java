package com.sky.controller.user;

import com.sky.dto.OrderDefult;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
public class AddressController {
    @Autowired
    private AddressService addressService;
//    新增地址
    @PostMapping()
    Result addAddress(@RequestBody AddressBook addressBook) {
        addressService.saveAddress(addressBook);
        return Result.success();
    }
//    修改地址
    @PutMapping()
    Result updateAddress(@RequestBody AddressBook addressBook) {
        addressService.updateAddress(addressBook);
        return Result.success();
    }
//    删除地址
    @DeleteMapping()
    Result deleteAddress(@RequestParam Long id) {
        addressService.deleteAddress(id);
        return Result.success();
    }
//    根据id查询
    @GetMapping("/{id}")
    Result<AddressBook> getAddressById(@PathVariable("id") Long id) {
        AddressBook res=addressService.getAddressById(id);
        return Result.success(res);
    }

//   获取所有地址
    @GetMapping("/list")
    Result<List<AddressBook>> getlist() {
        List<AddressBook> list= addressService.getlist();
        return Result.success(list);
    }
//    查询默认地址
    @GetMapping("/default")
    Result<AddressBook> getDefaultAddress() {
        AddressBook res=addressService.getDefaultAddress();
        return Result.success(res);
    }
//    设置默认地址
    @PutMapping("/default")
    Result setDefult(@RequestBody OrderDefult orderDefult) {
        addressService.setDefult(orderDefult);
        return Result.success();
    }
}
