package TestCases;

import static org.junit.Assert.*;

import org.junit.Test;

import TreeQWOP.ControlSequence;

public class SequenceTests {

	
	//Standard qwop sequence.
	boolean[][] testSequence1 = {
			{false,false,false,false},
			{false,true,false,true},
			{false,false,false,false},
			{true,false,true,false}
	};
	
	//Unusually-sized qwop sequence.
	boolean[][] testSequence2 = {
			{false,true,false,false},
			{false,true,false,true},
			{false,false,false,false},
			{true,false,true,false},
			{true,true,true,true}
	};
	
	@Test
	public void sequenceShouldWrap(){
		
		ControlSequence cs1 = new ControlSequence(testSequence1);
		for(int i = 0; i<testSequence1.length; i++){
			if(cs1.getNext() != testSequence1[i]){
				fail("First run through normal sequence does not match the given sequence");
			}		
			if(cs1.getPhase() != i){
				fail("Phase of normal sequence not correct (and zero indexed). Phase: " + cs1.getPhase() + ". Actual: " + i + ".");
			}
		}
		for(int i = 0; i<testSequence1.length; i++){
			if(cs1.getNext() != testSequence1[i]){
				fail("Wrapped run through normal sequence does not match the given sequence");
			}		
			if(cs1.getPhase() != i){
				fail("Phase of normal sequence not correct (and zero indexed). Phase: " + cs1.getPhase() + ". Actual: " + i + ".");
			}
		}	
		
		
		ControlSequence cs2 = new ControlSequence(testSequence2);
		for(int i = 0; i<testSequence2.length; i++){
			if(cs2.getNext() != testSequence2[i]){
				fail("First run through unusual sequence does not match the given sequence");
			}	
			if(cs2.getPhase() != i){
				fail("Phase of normal sequence not correct (and zero indexed). Phase: " + cs2.getPhase() + ". Actual: " + i + ".");
			}
		}
		for(int i = 0; i<testSequence2.length; i++){
			if(cs2.getNext() != testSequence2[i]){
				fail("Wrapped run through unusual sequence does not match the given sequence");
			}
			if(cs2.getPhase() != i){
				fail("Phase of normal sequence not correct (and zero indexed). Phase: " + cs2.getPhase() + ". Actual: " + i + ".");
			}
		}	
	}
	
	@Test
	public void sequenceLengthShouldMatchActions(){
		ControlSequence cs1 = new ControlSequence(testSequence1);
		ControlSequence cs2 = new ControlSequence(testSequence2);
		
		if (cs1.getActionCount() != testSequence1.length){
			fail("Sequence length doesn't agree.");
		}
		if (cs2.getActionCount() != testSequence2.length){
			fail("Sequence length doesn't agree.");
		}
	}
	

}
