package antifraud.repository;

import antifraud.entity.IpAddress;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("unused")
public interface IpAddressRepository extends CrudRepository<IpAddress,Long> {
    List<IpAddress> findAll();
}
