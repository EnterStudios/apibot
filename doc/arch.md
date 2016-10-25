# ApiBot Architechture

ApiBot is a visual language for describing stateful API flows.

## Modules

1. Requests:
   This module describes requests, e.g. POST /api/{api}/login which are the building
   blocks for every API interaction. A request has the following attributes:

   Name: the request's name
   Method: get/post/put/patch/delete
   Url: e.g. /api/{api}/user/login
   Body: (optional, depending on the request type)
   Headers: http headers.

2. EL (expression language):
   A very simple expression language allows parametrized bodies, headers and urls
   e.g you can declare a url for a request as /api/${api}/users/${user-id}.
   The ${} calls will be replaced by whatever value is stored inside the context.

   The sole purpose of the expression language is to extract values from the Context.

3. Context:
  The Context is a map of names to anything. Everything you store in the context is
  globally accessible by all requests.

4. Assertions:
  An assertion is simply a function which takes the response of a request and
  makes an assertion on the result.
  There are many kinds of assertions, some can take many responses as arguments.
  Fomally an assertion is a f(responses) -> Optional<Error>

5. Extractor:
  Extractors are functions which take the Context and one or many responses as argument
  and produce a key-value pair that will insert/replace whatever was previously mapped on
  the Context.

6. Flow Controls: TODO

7. Execution Graph:
An execution graph describes the execution of an api flow.
Nodes in the graph can be: Requests, assertions, extractors
Vertices are Flow  Controls.

8. Execution Engine:
The execution engine takes an excecution graph (or program) as argument
and will, every iteration, figure out which nodes it has to run, and run them.
For a node to be executed, two things must happen:
1. The arguments to that node must be possible (statically)
2. The arguments must be present right before the execution.
3. The execution pointer must be in the node.

After a node is executed, a Flow Control will indicate the execution engine
which node(s) should follow.
