# Execution Graph

The following is an example of an execution graph that
registers a user into a website and then logins.
Finally an assertion is made ensuring the login went
correctly.

#### Types of nodes:

1. Initialization:
There can be only one Initialization node in the graph. It indicates the 'main' or
starting point of the execution.

2. HTTP Request:
Executes an HTTP request and stores the response in the scope.

3. Assertion:
An assertion is a verification that halts the program if it does not succeed.

4. If/Branching:
A branching node reads values from the scope and decides which branch should execute.

5. Extractor:
An extractor reads values from the scope and saves it to the current session.

6. Termination:
A termination node halts the program.

#### Execution of a graph

The high level algorithm to execute a graph is as follows:

```
def execute_graph(g)
  # start with the init node
  node = g.init

  # On every step there can be at most 1 next_node
  while node.next_node
    # Every node knows which node should follow.
    node = node.next_node

    # each node has its own execution semantics. Some will
    # halt the program, others will perform an assertion,
    # others will make an http request.
    # The important thing to notice here is that execute!
    # will likely have side effects.
    node.execute!
  end
end
```
