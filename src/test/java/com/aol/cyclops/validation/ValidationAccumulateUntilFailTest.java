package com.aol.cyclops.validation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import lombok.Value;
public class ValidationAccumulateUntilFailTest {

	@Value
	static class User { 
		int age;
		String email;
	}
	@Test
	public void testAccumulateUntilFailSuccess() {
		ValidationResults<String,String> results  = Validator.of((User user)->user.age>18, "too young", "age ok")
												.add(Validator.of((User user)->user.email!=null, "user email null","email ok"))
												.accumulateUntilFail(new User(20,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
	}
	@Test
	public void testAccumulateUntilFailAtStart() {
		ValidationResults<String,String> results  = Validator.of((User user)->user.age>18, "too young", "age ok")
												.add(Validator.of((User user)->user.email!=null, "user email null","email ok"))
												.accumulateUntilFail(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(1));
	}
	
	
}
