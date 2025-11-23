### Діаграма класів реалізованої частини:

```mermaid
classDiagram
    direction TB

    class MindMapController {
        <<Controller>>
        -MindMapService mapService
        +saveMap(map: MindMap): MindMap
        +getMapsForUser(userId: String): List~MindMap~
    }

    class MindMapService {
        <<Service>>
        -MindMapRepository mapRepository
        +saveMapForUser(map: MindMap): MindMap
        +findMapsByUserId(userId: String): List~MindMap~
    }

    class MindMapRepository {
        <<Interface>>
        <<Repository>>
        +save(map: MindMap): MindMap
        +findByOwnerUserId(userId: String): List~MindMap~
    }
    
    class MindMap {
       <<Entity>>
       -String id
       -String title
       -String ownerUserId
       -Node rootNode
    }

    MindMapController ..> MindMapService : uses
    MindMapService ..> MindMapRepository : uses
    MindMapRepository ..> MindMap : manages