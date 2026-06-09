package com.smartbox.backend.config;

import com.smartbox.backend.model.Locker;
import com.smartbox.backend.repository.LockerRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class LockerInitializer {
    private final LockerRepository lockerRepository;

    @PostConstruct
    public void init() {

        if (lockerRepository.count() > 0) {
            return;
        }

        saveLocker("HN001","Ba Đình",21.0368,105.8342);
        saveLocker("HN002","Hoàn Kiếm",21.0285,105.8542);
        saveLocker("HN003","Tây Hồ",21.0700,105.8188);
        saveLocker("HN004","Long Biên",21.0419,105.8900);
        saveLocker("HN005","Cầu Giấy",21.0362,105.7908);
        saveLocker("HN006","Đống Đa",21.0180,105.8290);
        saveLocker("HN007","Hai Bà Trưng",21.0050,105.8570);
        saveLocker("HN008","Hoàng Mai",20.9733,105.8638);
        saveLocker("HN009","Thanh Xuân",20.9955,105.8097);
        saveLocker("HN010","Bắc Từ Liêm",21.0717,105.7610);
        saveLocker("HN011","Nam Từ Liêm",21.0123,105.7645);
        saveLocker("HN012","Hà Đông",20.980366,105.788613);
    }

    private void saveLocker(
            String id,
            String address,
            double lat,
            double lng) {

        Locker locker = new Locker();

        locker.setLockerId(id);
        locker.setAddress(address);

        locker.setLatitude(lat);
        locker.setLongitude(lng);

        locker.setStatus("ACTIVE");

        locker.setTotalSlots(5);
        locker.setAvailableSlots(5);

        locker.setSlots(Arrays.asList(
                "1","2","3","4","5"
        ));

        lockerRepository.save(locker);
    }
}
