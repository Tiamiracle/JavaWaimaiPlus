package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.OrderDefult;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressMapper;
import com.sky.service.AddressService;
import com.sky.utils.CheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressMapper addressMapper;

    @Override
    public void saveAddress(AddressBook addressBook) {
        Long userId= BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBook.setIsDefault(0);
        CheckUtil.checkPhone(addressBook.getPhone());
        addressMapper.save(addressBook);
    }

    @Override
    public void updateAddress(AddressBook addressBook) {
        addressBook.setIsDefault(0);
        CheckUtil.checkPhone(addressBook.getPhone());
        addressMapper.update(addressBook);
    }

    @Override
    public void deleteAddress(Long id) {
        addressMapper.delete(id);
    }

    @Override
    public AddressBook getAddressById(Long id) {
        return addressMapper.getAddressById(id);
    }

    @Override
    public List<AddressBook> getlist() {
        Long userId= BaseContext.getCurrentId();
        List<AddressBook>res=addressMapper.getlist(userId);
        return res;
    }

    @Override
    public AddressBook getDefaultAddress() {
        Long userId= BaseContext.getCurrentId();
        AddressBook res=addressMapper.getDefaultAddress(userId);
        return res;
    }

    @Override
    public void setDefult(OrderDefult orderDefult) {
        Long userId= BaseContext.getCurrentId();
        addressMapper.cleanDefault(userId);
        AddressBook address=addressMapper.getAddressById(orderDefult.getId());
        address.setIsDefault(1);
        addressMapper.update(address);
    }
}
