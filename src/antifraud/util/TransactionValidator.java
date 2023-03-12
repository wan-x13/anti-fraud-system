package antifraud.util;

import antifraud.entity.Card;
import antifraud.entity.Transaction;
import antifraud.repository.CardRepository;
import antifraud.repository.IpAddressRepository;
import antifraud.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class TransactionValidator {
    private final IpAddressRepository addressRepository;
    private final CardRepository cardRepository;
    private final Set<String> info = new TreeSet<>();
    private final List<String> transactionResult = new ArrayList<>();
    private final TransactionRepository transactionRepository;

    public TransactionValidator(IpAddressRepository addressRepository, CardRepository cardRepository, TransactionRepository transactionRepository) {
        this.addressRepository = addressRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
    }

    public Map<String , String> validTransaction(Transaction transaction){
         ifSuspiciousIp(transaction.getIp());
         ifStolenCard(transaction.getNumber());
         checkCorrelations(transaction);
         checkAmount(transaction);
         formatInfo();
         Map<String , String> response = new LinkedHashMap<>();
         response.put("result",transactionResult.get(0));
         response.put("info", formatInfo());
         transaction.setResult(transactionResult.get(0));

         transactionRepository.save(transaction);
         info.clear();
         transactionResult.clear();
         return response;
    }
    public void ifSuspiciousIp(String ip){
        if(addressRepository.findAll()
                .stream().anyMatch(it->it.getIp().equals(ip))){
            info.clear();
            transactionResult.add("PROHIBITED");
            info.add("ip");
        }
    }
    public void ifStolenCard(String number){
        if(cardRepository.existsByNumberAndIsLockedTrue(number)){
            transactionResult.add("PROHIBITED");
            info.add("card-number");
        }
    }

    public void checkCorrelations(Transaction transaction){

      var corrRegion=  findALlTransaction(transaction).stream()
              .map(Transaction::getRegion)
              .filter(it->!it.equals(transaction.getRegion()))
              .distinct()
              .count();
      var corrIp = findALlTransaction(transaction).stream()
                      .map(Transaction::getIp).filter(it->!it.equals(transaction.getIp()))
                      .distinct().count();

      if(corrRegion == 2 && !transactionResult.contains("PROHIBITED")){
          transactionResult.add("MANUAL_PROCESSING");
          info.add("region-correlation");
      }
      if(corrIp == 2 && !transactionResult.contains("PROHIBITED")){
          transactionResult.add("MANUAL_PROCESSING");
          info.add("ip-correlation");
       }
       if(corrRegion > 2){
          transactionResult.add("PROHIBITED");
          info.add("region-correlation");
       }
        if(corrIp > 2){
            transactionResult.add("PROHIBITED");
            info.add("ip-correlation");
        }
    }
    public void checkAmount(Transaction transaction){
        Optional<Card> optionalCard= cardRepository.findAll().stream()
                .filter(it-> it.getNumber().equals(transaction.getNumber()))
                .findAny();

        if(optionalCard.isPresent()) {
            Card card = optionalCard.get();
            int maxAllowed = card.getMaxAllowed();
            int maxManual = card.getMaxManual();
            if ((transaction.getAmount() > maxAllowed && transaction.getAmount() <= maxManual)
                    && (!transactionResult.contains("PROHIBITED"))) {
                transactionResult.add("MANUAL_PROCESSING");
                info.add("amount");
            } else if (transaction.getAmount() > 0 && transaction.getAmount() <= maxAllowed &&
                    (!transactionResult.contains("PROHIBITED") && !transactionResult.contains("MANUAL_PROCESSING"))) {
                transactionResult.add("ALLOWED");
                info.add("none");
            } else if (transaction.getAmount() > maxManual) {

                transactionResult.add("PROHIBITED");
                info.add("amount");
            }
        }
    }
    public long findAllTimeBetween(List<Transaction> list,Transaction transaction){
     return  list.stream()
                .filter(it->it.getDate().getDayOfMonth() == transaction.getDate().getDayOfMonth())
                .map(it->it.getDate().minusHours(1))
                .filter(it->it.plusHours(1) == transaction.getDate())
                .count();
    }
    public List<Transaction> findALlTransaction(Transaction transaction){
      return  transactionRepository.findAllByDateBetweenAndNumber(
              transaction.getDate().minusHours(1),
              transaction.getDate(),
              transaction.getNumber()
      );
    }
    public String formatInfo(){
        return String.join(", ", info);
    }

}
