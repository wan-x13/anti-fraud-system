package antifraud.controller;


import antifraud.Account;



import antifraud.entity.Card;
import antifraud.entity.IpAddress;
import antifraud.entity.Transaction;
import antifraud.exception.BadRequestException;
import antifraud.service.CardService;
import antifraud.service.IpAddressService;
import antifraud.service.TransactionService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/antifraud")
public class AntiFraudController {
    private final IpAddressService ipAddressService;
    private final CardService cardService ;
    private final TransactionService transactionService;
    @PostMapping("/transaction")
    public ResponseEntity<?> transaction(@Validated @RequestBody Transaction transaction){
        var res = transactionService.addTransaction(transaction);
        return  ResponseEntity.ok(res);
    }


    @PostMapping("/suspicious-ip")
    public ResponseEntity<?> addSuspiciousIP(@RequestBody  IpAddress ipAddress){
        ipAddressService.setSuspiciousIpAddress(ipAddress);
        return ResponseEntity.ok(ipAddress);
    }
    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<?> deleteSuspiciousIP(@PathVariable String ip){
        ipAddressService.deleteSuspiciousIpAddress(ip);
        return ResponseEntity.ok(Map.of(
                "status", "IP "+ ip+" successfully removed!"
        ));
    }
    @GetMapping("/suspicious-ip")
    public List<IpAddress> getIpAddresses(){
        return ipAddressService.getAllIpAddresses();
    }
    @PostMapping("/stolencard")
    public ResponseEntity<?> addStolenCard(@RequestBody Card card){
        cardService.setStolenCard(card);
        return ResponseEntity.ok(card);
    }
    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<?> deleteCard(@PathVariable String number){
        cardService.removeStolenCard(number);
        return ResponseEntity.ok(Map.of(
                "status","Card "+ number+" successfully removed!"
        ));
    }
    @GetMapping("/stolencard")
    public List<Card> getAllCards(){
        return cardService.getAllStolenCard();
    }
    @PutMapping(value = "/transaction", consumes = "application/json")
    public ResponseEntity<?> addFeedback(@RequestBody Map<String , String> transactionFeedBack){
        long transactionId = Long.parseLong(transactionFeedBack.get("transactionId"));
        String feedback = transactionFeedBack.get("feedback");

      Transaction transaction = transactionService.addFeedback(transactionId, feedback);
      return ResponseEntity.ok(transaction);
    }
    @GetMapping("/history")
    public List<Transaction> getAllHistory(){
        return transactionService.getAllTransaction();
    }
    @GetMapping("/history/{number}")
    public List<Transaction> getAllTransactionByNumber(@PathVariable String number){
        return transactionService.getAllTransactionByCardNumber(number);
    }
}
