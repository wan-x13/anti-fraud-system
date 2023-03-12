package antifraud.service;


import antifraud.entity.Card;

import antifraud.exception.BadRequestException;
import antifraud.exception.ConflictException;
import antifraud.exception.NotFoundException;
import antifraud.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CardService {
    private final CardRepository cardRepository;
    @Autowired
    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> getAllStolenCard(){
        return cardRepository.findAllByIsLockedTrue().stream()
                .toList();
    }
    public  void removeStolenCard(String number){
        if(!luhnAlgorithm(number)){
            throw new BadRequestException();
        }
        else if(cardRepository.findAll().stream()
                .noneMatch(u-> Objects.equals(u.getNumber(), number))){
            throw new NotFoundException();
        }else {
           Optional<Card>  cardFind = cardRepository.findAllByIsLockedTrue().stream()
                    .filter(it->it.getNumber().equals(number) )
                    .findAny();
           assert cardFind.isPresent();
           cardRepository.delete(cardFind.get());
        }
    }

    public void setStolenCard(Card card){
        if(!luhnAlgorithm(card.getNumber())){
            throw new BadRequestException();
        }
        else if(cardRepository.findAllByIsLockedTrue().stream()
                .anyMatch(u-> Objects.equals(u.getNumber(), card.getNumber()))){
            throw new ConflictException();
        }
        else{
            card.setNumber(card.getNumber());
            card.setLocked(true);
            cardRepository.save(card);
        }
    }

    public boolean luhnAlgorithm(String accountNumber){
        var list = new ArrayList<>(Arrays.stream(String.valueOf(accountNumber).split(""))
                .map(Integer::parseInt)
                .toList());
        int lastElement = list.get(list.size()-1);
        list.remove(list.size()-1);
        int sum = 0;
        for(int i = list.size()-1; i >= 0; i--){
            int temp = i%2 == 0 ? list.get(i)*2 : list.get(i);
            sum += temp > 9 ? temp -9 : temp;
        }
        return  (sum + lastElement) % 10 == 0;
    }
}
