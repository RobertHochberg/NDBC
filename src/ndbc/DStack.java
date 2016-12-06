package ndbc;

import java.util.ArrayList;

public class DStack extends ArrayList<Float> {

	public DStack(){
		super(20);
		
		for(int i=0;i<20;i++)
			add(0f);
	}
	
	public void addMessage(float one, float five, float twenty){
		if(get(0) != one)
			pop();
		
		set(0, one);
		set(4, five);
		set(19, twenty);
	}
	
	public void addMessage(Float[] values){
		set(0, values[0]);
		set(4, values[1]);
		set(19, values[2]);
	}
	
	public float pop(){
		float rv = get(0);
		
		for(int i=0; i<size()-1; i++)
			set(i, get(i+1));
		
		set(19,0f);
		
		return rv;
	}
	
	public float peek(){
		return get(0);
	}
	
	public float peek2(){
		return get(1);
	}
	
	public boolean buy(){
		return peek() < peek2();
	}
	
}
