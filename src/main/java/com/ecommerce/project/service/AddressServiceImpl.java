package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);

        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);

        address.setUsers(user);
        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addressList = addressRepository.findAll();
        List<AddressDTO> addressDTOList = addressList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
        return addressDTOList;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(
                () -> new ResourceNotFoundException("Address", "addressId", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddressesByLoggedInUser(User user) {
        List<Address> addressList = user.getAddresses();
        List<AddressDTO> addressDTOList = addressList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
        return addressDTOList;
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {

        Address addressFromDatabase = addressRepository.findById(addressId).orElseThrow(
                () -> new ResourceNotFoundException("Address", "addressId", addressId));

        addressFromDatabase.setStreet(addressDTO.getStreet());
        addressFromDatabase.setCity(addressDTO.getCity());
        addressFromDatabase.setState(addressDTO.getState());
        addressFromDatabase.setPincode(addressDTO.getPincode());
        addressFromDatabase.setCountry(addressDTO.getCountry());
        addressFromDatabase.setBuildingName(addressDTO.getBuildingName());
        addressRepository.save(addressFromDatabase);

        User user = addressFromDatabase.getUsers();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(addressFromDatabase);
        userRepository.save(user);

        return modelMapper.map(addressFromDatabase, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address addressFromDatabase = addressRepository.findById(addressId).orElseThrow(
                () -> new ResourceNotFoundException("Address", "addressId", addressId));

        User user = addressFromDatabase.getUsers();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.deleteById(addressId);

        return "Address with id " + addressId + " deleted successfully.";
    }
}
