package com.sky.service;

import com.sky.dto.OrderDefult;
import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressService {
    void saveAddress(AddressBook addressBook);

    void updateAddress(AddressBook addressBook);

    void deleteAddress(Long id);

    AddressBook getAddressById(Long id);

    List<AddressBook> getlist();

    AddressBook getDefaultAddress();

    void setDefult(OrderDefult orderDefult);
}
