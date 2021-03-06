package scalaparser

import org.parboiled2._

trait L5_Types { this: Parser with WhitespaceStringsAndChars
  with L0_Basics
  with L1_KeywordsAndOperators
  with L2_Identifiers
  with L3_Literals
  with L4_Core =>

  def TypeExpr: Rule0

  def Mod: Rule0 = rule( LocalMod | AccessMod | `override` )
  def LocalMod: Rule0 = rule( `abstract` | `final` | `sealed` | `implicit` | `lazy` )
  def AccessMod: Rule0 = {
    def AccessQualifier = rule( '[' ~ (`this` | WLId) ~ ']' )
    rule( (`private` | `protected`) ~ AccessQualifier.? )
  }

  def Dcl: Rule0 = {
    def VarDcl = rule( `var` ~ WLIds ~ `:` ~ Type )
    def FunDcl = rule( `def` ~ FunSig ~ (`:` ~ Type).? )
    rule( ValDcl | VarDcl | FunDcl | TypeDcl )
  }

  def Type: Rule0 = {
    def FunctionArgTypes = rule('(' ~ ParamType.+(',').? ~ ')' )
    def ArrowType = rule( FunctionArgTypes ~ `=>` ~ Type )
    def ExistentialClause = rule( `forSome` ~ `{` ~ (TypeDcl | ValDcl).+(Semis) ~ `}` )
    def PostfixType = rule( InfixType ~ (`=>` ~ Type | ExistentialClause.?) )
    def Unbounded = rule( `_` | ArrowType | PostfixType )
    rule( Unbounded ~ TypeBounds )
  }

  def InfixType = rule( CompoundType ~ (NotNewline ~ WLId ~ OneNLMax ~ CompoundType).* )

  def CompoundType = {
    def RefineStat = rule( TypeDef | Dcl  )
    def Refinement = rule( OneNLMax ~ `{` ~ RefineStat.*(Semis) ~ `}` )
    rule( AnnotType.+(`with`) ~ Refinement.? | Refinement )
  }
  def AnnotType = rule(SimpleType ~ (NotNewline ~ (NotNewline ~ Annot).+).? )

  def SimpleType: Rule0 = {
    def BasicType = rule( '(' ~ Types ~ ')'  | StableId ~ '.' ~ `type` | StableId )
    rule( BasicType ~ (TypeArgs | `#` ~ WLId).* )
  }

  def TypeArgs = rule( '[' ~ Types ~ "]" )
  def Types = rule( Type.+(',') )

  def ValDcl: Rule0 = rule( `val` ~ WLIds ~ `:` ~ Type )
  def TypeDcl: Rule0 = rule( `type` ~ WLId ~ TypeArgList.? ~ TypeBounds )

  def FunSig: Rule0 = {
    def FunTypeArgs = rule( '[' ~ (Annot.* ~ TypeArg).+(',') ~ ']' )
    def FunAllArgs = rule( FunArgs.* ~ (OneNLMax ~ '(' ~ `implicit` ~ Args ~ ')').? )
    def FunArgs = rule( OneNLMax ~ '(' ~ Args.? ~ ')' )
    def FunArg = rule( Annot.* ~ WLId ~ (`:` ~ ParamType).? ~ (`=` ~ TypeExpr).? )
    def Args = rule( FunArg.+(',') )
    rule( (WLId | `this`) ~ FunTypeArgs.? ~ FunAllArgs )
  }
  def ParamType = rule( `=>` ~ Type | Type ~ "*" | Type )

  def TypeBounds: Rule0 = rule( (`>:` ~ Type).? ~ (`<:` ~ Type).? )
  def TypeArg: Rule0 = {
    def CtxBounds = rule((`<%` ~ Type).* ~ (`:` ~ Type).*)
    rule((WLId | `_`) ~ TypeArgList.? ~ TypeBounds ~ CtxBounds)
  }

  def Annot: Rule0 = rule( `@` ~ SimpleType ~  ('(' ~ (Exprs ~ (`:` ~ `_*`).?).? ~ ")").*  )

  def TypeArgList: Rule0 = {
    def Variant: Rule0 = rule( Annot.* ~ (WL ~ anyOf("+-")).? ~ TypeArg )
    rule( '[' ~ Variant.*(',') ~ ']' )
  }
  def Exprs: Rule0 = rule( TypeExpr.+(',') )
  def TypeDef: Rule0 = rule( `type` ~ WLId ~ TypeArgList.? ~ `=` ~ Type )
}
