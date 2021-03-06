package FAtiMA.conditions;

import java.util.ArrayList;

import org.xml.sax.Attributes;


import FAtiMA.AgentModel;
import FAtiMA.exceptions.ContextParsingException;
import FAtiMA.exceptions.InvalidEmotionTypeException;
import FAtiMA.memory.semanticMemory.KnowledgeBase;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.wellFormedNames.Symbol;

/**
 * Represents a Like Relation that needs to be fullfiled in order to trigger
 * the condition.
 * The xml should be defined as <LikeRelation SUBJECT_STR="subject_name" TARGET_STR="character_name" OPERATOR_STR="operator" VALUE_STR="relation_value"/>,
 * for example, by default: <LikeRelation subject="Luke" target="John" operator=">" value="3"/>.
 * 		A target must be a character;
 * 		The value must be an integer in the range [-10;10]; 
 * 		Operator can be one of the following < <= = >= > !=
 * @author nafonso
 * @see Context
 * @see Ritual
 */
public class LikeCondition extends Condition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Operator LESS_THAN = new LessThan();
	private static Operator LESS_THAN_OR_EQUAL = new LessThanOrEqual();
	private static Operator EQUAL = new Equal();
	private static Operator MORE_THAN_OR_EQUAL = new MoreThanOrEqual();
	private static Operator MORE_THAN = new MoreThan();
	private static Operator NOT_EQUAL = new NotEqual();
	
	private Operator _operator;
	private float _value;
	
	public static LikeCondition ParseSocialCondition(Attributes attributes) throws InvalidEmotionTypeException, ContextParsingException {
		LikeCondition sc;
		Symbol subject;
		Symbol target = null;
		Operator op;
		float value = 0;
		String aux;
		
		
		aux = attributes.getValue("subject");
		if(aux == null)
		{
			subject = Constants.UNIVERSAL;
		}
		else
		{
			subject = new Symbol(aux);
		}
		
		
		aux = attributes.getValue("target");
		if(aux != null)
		{
			target = new Symbol(aux);
		}
	
		aux = attributes.getValue("operator");
		op = LikeCondition.parseOperator(aux);

		aux = attributes.getValue("value");
		if(aux != null)
		{
			value = Float.parseFloat(aux);	
		}
		
		sc = new LikeCondition(subject,target,value,op);
			
		return sc;
	}
	
	private LikeCondition()
	{
		
	}

	protected LikeCondition(Symbol subject, Symbol target, float value, Operator op){
		this._ToM = subject;
		this._name = target;
		this._value = value;
		this._operator = op;
	}
	
	public Object clone()
	{
		LikeCondition cond = new LikeCondition();
		cond._ToM = (Symbol) this._ToM.clone();
		cond._name = (Symbol) this._name.clone();
		cond._value = this._value;
		cond._operator = this._operator;
		return cond;
	}
	
	
	public boolean CheckCondition(AgentModel am) {
		float existingValue;
		
		if(!this.isGrounded()) return false;
		
		AgentModel perspective = this.getPerspective(am);
		
		
		existingValue = LikeRelation.getRelation(Constants.SELF, _name.toString()).getValue(perspective.getMemory());
		
		return _operator.process(existingValue, _value);
	}
	
	
	public String toString()
	{
		return _ToM + " like" + _operator + " " + _name + " " + _value;
	}
	
	private static ContextParsingException createException( String msg ){
		return new ContextParsingException("SocialRelationCondition: "+msg);
	}
	
	private static Operator parseOperator( String operator ) throws ContextParsingException{
		if( operator == null )
			throw new ContextParsingException("No operator was found in SocialRelationCondition");
		Operator auxOp;
		if(operator.equals("LesserThan"))
			auxOp = LESS_THAN; //Operator.LESS_THAN;
		else if(operator.equals("LesserEqual"))
			auxOp = LESS_THAN_OR_EQUAL;
		else if(operator.equals("="))
			auxOp = EQUAL;
		else if(operator.equals("GreaterEqual"))
			auxOp = MORE_THAN_OR_EQUAL;
		else if(operator.equals("GreaterThan"))
			auxOp = MORE_THAN;
		else if(operator.equals("!="))
			auxOp = NOT_EQUAL;
		else
			throw new ContextParsingException("Invalid operator '"+operator+"' found in SocialRelationCondition");
		return auxOp;
	}
	
	private interface Operator{
		public abstract boolean process( float val1, float val2 );
	}
	
	private static class LessThan implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 < val2;
		}
		
		public String toString()
		{
			return "<";
		}
	}
	
	private static class LessThanOrEqual implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 <= val2;
		}
		
		public String toString()
		{
			return "<=";
		}
	}
	
	private static class Equal implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 == val2;
		}
		
		public String toString()
		{
			return "=";
		}
	}
	
	private static class MoreThanOrEqual implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 >= val2;
		}
		
		public String toString()
		{
			return ">=";
		}
	}
	
	private static class MoreThan implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 > val2;
		}
		
		public String toString()
		{
			return ">";
		}
	}
	
	private static class NotEqual implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 != val2;
		}
		
		public String toString()
		{
			return "!=";
		}
	}

	@Override
	public Name GetValue() {
		return new Symbol(String.valueOf(this._value));
	}

	@Override
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		ArrayList<SubstitutionSet> bindingSets = new ArrayList<SubstitutionSet>();
		
		if(!this._ToM.isGrounded()) return null;
		
		if(this._name.isGrounded())
		{
			if(CheckCondition(am))
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		
		AgentModel perspective = this.getPerspective(am);
		
		Name likeProperty = Name.ParseName("Like(" + Constants.SELF + "," + this._name.toString() + ")");
		bindingSets = perspective.getMemory().getSemanticMemory().GetPossibleBindings(likeProperty);
		
		return bindingSets;
	}

	@Override
	public Object GenerateName(int id) {
		LikeCondition cond = (LikeCondition) this.clone();
		cond.ReplaceUnboundVariables(id);
		return cond;
	}

	@Override
	public Object Ground(ArrayList<Substitution> bindingConstraints) {
		LikeCondition cond = (LikeCondition) this.clone();
		cond.MakeGround(bindingConstraints);
		return cond;
	}

	@Override
	public Object Ground(Substitution subst) {
		LikeCondition cond = (LikeCondition) this.clone();
		cond.MakeGround(subst);
		return cond;
	}

	@Override
	public void MakeGround(ArrayList<Substitution> bindings) {
		this._ToM.MakeGround(bindings);
		this._name.MakeGround(bindings);	
	}

	@Override
	public void MakeGround(Substitution subst) {
		this._ToM.MakeGround(subst);
		this._name.MakeGround(subst);	
	}

	@Override
	public void ReplaceUnboundVariables(int variableID) {
		this._ToM.ReplaceUnboundVariables(variableID);
		this._name.ReplaceUnboundVariables(variableID);
	}

	@Override
	public boolean isGrounded() {
		return this._ToM.isGrounded() && this._name.isGrounded();
	}

	@Override
	protected ArrayList<Substitution> GetValueBindings(AgentModel am) {
		// TODO Auto-generated method stub
		return null;
	}
}
