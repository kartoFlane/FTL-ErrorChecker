The ErrorChecker uses BeanShell2 as the scripting language.

BeanShell is basically like Java, but less strict, allowing you
to use untyped variables if you want to.

Theoretically, you could be able to write Java code and BeanShell
should understand it. However, since BeanShell is not yet complete
(and probably won't be anytime soon), it doesn't understand some
structures, such as enums.

================================================================
BeanShell manual:
http://www.beanshell.org/manual/quickstart.html

The BeanShell2 fork (last active Feb 2014 as of time of writing):
https://code.google.com/p/beanshell2/

Last hope:
https://www.google.com/#q=beanshell
