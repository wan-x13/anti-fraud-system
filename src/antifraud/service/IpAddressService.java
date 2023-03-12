package antifraud.service;



import antifraud.entity.IpAddress;
import antifraud.exception.BadRequestException;
import antifraud.exception.ConflictException;
import antifraud.exception.NotFoundException;
import antifraud.repository.IpAddressRepository;
import com.google.common.net.InetAddresses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class IpAddressService {
    private final IpAddressRepository ipAddressRepository;
    @Autowired
    public IpAddressService(IpAddressRepository ipAddressRepository) {
        this.ipAddressRepository = ipAddressRepository;
    }

    public List<IpAddress> getAllIpAddresses(){
        return ipAddressRepository.findAll()
                .stream().toList();
    }

    public void setSuspiciousIpAddress(IpAddress ipAddress){
        if(!isValidIpAddress(ipAddress.getIp())){

            throw new BadRequestException();
        } else if (ipAddressRepository.findAll().stream()
                .anyMatch(it-> Objects.equals(it.getIp(), ipAddress.getIp()))) {
            throw new ConflictException();
        }
        ipAddressRepository.save(ipAddress);
    }
    public void deleteSuspiciousIpAddress(String ip){
        if (!isValidIpAddress(ip)) {

            throw new BadRequestException();
        }
        if(ipAddressRepository.findAll().stream()
                .noneMatch(it-> Objects.equals(it.getIp(), ip))){
            throw  new NotFoundException();
        }
        Optional<IpAddress> ipFind = ipAddressRepository.findAll().stream()
                .filter(it-> Objects.equals(it.getIp(), ip))
                .findAny();
        assert  ipFind.isPresent();
        ipAddressRepository.delete(ipFind.get());
    }
    public boolean isValidIpAddress(String ipAddress){
       return InetAddresses.isInetAddress(ipAddress);
    }

}
