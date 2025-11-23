Діаграма класів предметної області:

```mermaid
classDiagram
    direction LR

    class MindMap {
        <<Entity>>
        -String id
        -String title
        -String ownerUserId
        -Node rootNode
    }

    class Node {
        -String id
        -String text
        -double x
        -double y
        -List~Node~ children
    }

    MindMap "1" *-- "1" Node : has root
    Node "1" *-- "0..*" Node : has children