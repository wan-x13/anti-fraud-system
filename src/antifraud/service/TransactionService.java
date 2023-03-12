package antifraud.service;

import antifraud.entity.Card;
import antifraud.entity.Transaction;
import antifraud.repository.CardRepository;
import antifraud.repository.TransactionRepository;
import antifraud.util.TransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final CardService cardService;
    private final IpAddressService ipAddressService;
    private final TransactionValidator transactionValidator;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;


    @Autowired
    public TransactionService(CardService cardService, IpAddressService ipAddressService, TransactionValidator transactionValidator, TransactionRepository transactionRepository, CardRepository cardRepository) {
        this.cardService = cardService;
        this.ipAddressService = ipAddressService;
        this.transactionValidator = transactionValidator;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
    }

    public Map<String, String> addTransaction(Transaction transaction){
        List<String> code = List.of("EAP" , "ECA","HIC","LAC","MENA","SA","SSA");
       String ip = transaction.getIp() != null ?  ipAddressService.isValidIpAddress(transaction.getIp())?
               transaction.getIp() : null : null;
       String number = transaction.getNumber() != null ? cardService.luhnAlgorithm(transaction.getNumber()) ?
               transaction.getNumber() : null : null ;
       String region =transaction.getRegion() != null ? code.contains(transaction.getRegion()) ? transaction.getRegion() : null : null;

        if(transaction.getAmount() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
       if(ip == null || number == null || region == null ||
         ip.isEmpty() || number.isEmpty() || region.isEmpty() ||
               transaction.getAmount() < 1 ){
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
       }
       if(!cardRepository.existsByNumberAndIsLockedTrue(number)){
           Card card = new Card();
           card.setNumber(number);
           card.setLocked(false);
           cardRepository.save(card);
       }
      return transactionValidator.validTransaction(transaction);

    }
    public Transaction addFeedback(Long transactionId , String feedBack){
        List<String> validate = List.of("ALLOWED","MANUAL_PROCESSING","PROHIBITED");

        var transaction = transactionRepository.findById(transactionId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(!validate.contains(feedBack)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        else if(!transaction.getFeedBack().isBlank()){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        else if( transaction.getResult().equals(feedBack) ){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        changeLimit(transaction, feedBack);

        transaction.setFeedBack(feedBack);
        transactionRepository.save(transaction);
      return transaction;
    }
    public List<Transaction> getAllTransaction(){
        return  transactionRepository.findAll().stream()
                .toList();
    }
    public List<Transaction> getAllTransactionByCardNumber(String number){
        if(!cardService.luhnAlgorithm(number)){
           throw new  ResponseStatusException(HttpStatus.BAD_REQUEST) ;
        }
        var list =   transactionRepository.findByNumber(number)
                .stream().toList();
        if(list.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return list;
    }
    private void changeLimit(Transaction transaction, String feedback) {
        String trResult = transaction.getResult();

        Card card = cardRepository.findAll().stream()
                .filter(it-> !it.isLocked() && it.getNumber().equals(transaction.getNumber()))
                .findAny().orElseThrow(AssertionError::new);


        // Formula for increasing the limit: new_limit = 0.8 * current_limit + 0.2 * value_from_transaction
        int increasedAllowed = (int) Math.ceil(0.8 * card.getMaxAllowed() + 0.2 * transaction.getAmount());
        int decreasedAllowed = (int) Math.ceil(0.8 * card.getMaxAllowed() - 0.2 * transaction.getAmount());
        int increasedManual = (int) Math.ceil(0.8 * card.getMaxManual() + 0.2 * transaction.getAmount());
        int decreasedManual = (int) Math.ceil(0.8 * card.getMaxManual() - 0.2 * transaction.getAmount());

        // Set the new limit based on the feedback
        if (feedback.equals("MANUAL_PROCESSING") && trResult.equals("ALLOWED")) {
            card.setMaxAllowed(decreasedAllowed);
        } else if (feedback.equals("PROHIBITED") && trResult.equals("ALLOWED")) {
            card.setMaxAllowed(decreasedAllowed);
            card.setMaxManual(decreasedManual);
        } else if (feedback.equals("ALLOWED") && trResult.equals("MANUAL_PROCESSING")) {
            card.setMaxAllowed(increasedAllowed);
        } else if (feedback.equals("PROHIBITED") && trResult.equals("MANUAL_PROCESSING")) {
            card.setMaxManual(decreasedManual);
        } else if (feedback.equals("ALLOWED") && trResult.equals("PROHIBITED")) {
            card.setMaxAllowed(increasedAllowed);
            card.setMaxManual(increasedManual);
        } else if (feedback.equals("MANUAL_PROCESSING") && trResult.equals("PROHIBITED")) {
            card.setMaxManual(increasedManual);
        }

        // Save the new limit in the database
        cardRepository.save(card);
    }

}

