package edu.kit.minijava.transformation;

import edu.kit.minijava.ast.nodes.TypeReference;
import firm.Type;

public class EntityContext {

		private Type type;
		
		private TypeReference reference;
		
		public EntityContext() {
		}
		
		public Type getType() {
			return type;
		}
		
		public void setType(Type type) {
			this.type = type;
		}
	
	
}
