Json is a data description format, kinda like XML, but less bloated.

It is based on named nodes called fields, which correspond to tags
in XML, except they come in three different forms.
A node can be either:
 - a value node, which stores a value (like a number)
 - an array node, which contains a list of different values,
 - an object node, in which other nodes are nested.

There are three types of valid values in Json:
 - string (text surrounded by quotes),
 - number (either integer or real),
 - boolean (true or false).
There's also null, which indicates a void reference, but you probably
won't be using it here much.

----------------------------------------------------------------------
Syntax example:

{
	"string_value_node":	"my_value",		// <--- comma separates nodes
	"number_value_node1":	5,
	"number_value_node2":	1.2,
	"boolean_value_node":	true,
	"null_value_node":		null,

	"array_node": [
		"val_1",	// <--- comma also separates array/object node entries
		7,
		false,
		"val_2"		// <--- no comma if last
	],

	"object_node": {
		"child_node": "whatever"			// <--- last, hence no comma
	}				// <--- no comma here either
}

A single node consists of the node's name, followed by colon ':', and then
either the node's value, square brackets [] (for array nodes) or curly
braces {} (for object nodes).
Keep in mind that all nodes have to be separated by a comma ',' (there's no
comma after the last node in brackets/braces)

...So that about covers everything you need to know about Json


