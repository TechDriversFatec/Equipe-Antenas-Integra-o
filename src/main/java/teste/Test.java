package teste;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

class Test {

	public String senha;
	public String email;
	
	@org.junit.jupiter.api.Test
	void test() {
		
		email = "teste";
		senha = "123";
		
		assertNotNull(email);
		assertNotNull(senha);
			
	}

}
