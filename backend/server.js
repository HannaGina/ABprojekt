const net = require('net')
const fs = require('fs');
const { MongoClient, ServerApiVersion } = require('mongodb');
const uri = "mongodb+srv://abuser:OLb1hZPcnBK4bJEr@abprojekt.4qafu.mongodb.net/test6?retryWrites=true&w=majority";
const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });


const uri2 = "mongodb+srv://abuser:Akhnjofxy5QEoF8P@indexcluster.niofn.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
const indexClient = new MongoClient(uri2, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });


const port = 2500;

function createDatabase(value){
    if (!fs.existsSync(`databases/${value}`)){
        fs.mkdirSync(`databases/${value}`);
        client.db(value).collection("temp").in;          
        return "OK";
    }
    else{
        return "Letezik mar az adatbazis.";
    }
}

function getDatabases(){
    return fs.readdirSync("databases/", { withFileTypes: true })
        .filter(dir => dir.isDirectory())
        .map(dir => dir.name)
}

function dropDatabase(value){
    if(fs.existsSync(`databases/${value}`)){
        fs.rmSync(`./databases/${value}`,  { recursive: true, force: true });
        client.db(value).dropDatabase();
        indexClient.db(value).dropDatabase();

        return 'OK';
    }
    else{
        return 'Nem letezik az adatbazis.';
    }
}

function createTable(value){
    let fname = `databases/${value.database}/${value.table}/${value.table}.json`;
    let atrributeNames = value.attributes.map(a => a.name);
    let areAtrributeNamesUnique = atrributeNames.every((e, i) => atrributeNames.indexOf(e) == i);
    if(value.table === ''){
        return "A tablanak kell nevet adni.";
    }
    else if(!areAtrributeNamesUnique){
        return "Tobb ugyanolyan nevu attributum van.";
    }
    else if(atrributeNames.some(e => e == '')){
        return "Van ures mezo.";
    }
    else if(atrributeNames.some(e => /,/.test(e))){
        return "Az egyik mezo vesszot tartalmaz."
    }
    else if(value.attributes.filter(e => e.pk).length != 1){
        return "Kell pontosan egy primary key!";
    }
    else if(fs.existsSync("databases/" + value.database) && !fs.existsSync(fname)){
        value.attributes.map(e => {if(e.pk){e.index = true; e.unique = true;}});
        value.attributes.map(e => {if(e.ftable === '' || e.ftable === null){e.fk = false}});
        value.attributes.map(e => {if(e.unique){e.index = true}});
        

        fs.mkdirSync(`databases/${value.database}/${value.table}`, (err) =>{
            return "Letezik a tabla";
        });
        fs.writeFileSync(fname, JSON.stringify(value.attributes, null, 4), { flag: 'wx' });
        client.db(value.database).createCollection(value.table);
        return "OK";
    }
    else{
        return "Letezik mar ez a tabla.";
    }
}

function getTables(value){
    return fs.readdirSync(`databases/${value}/`, { withFileTypes: true })
        .map(file => file.name.replace("", ""))
}

async function dropTable(value){
    
    if(value.table == '' || value.table == null){
        return "Nincs neve a tablanak";
    }
    let fname = `databases/${value.database}/${value.table}`;
    if(fs.existsSync(fname)){
        let somethingDependsOnThis = false;
        fs.readdirSync(`databases/${value.database}`).forEach(dir =>{
            const attributes = require(`./databases/${value.database}/${dir}/${dir}.json`)
            if(attributes.some(a => a.fk && (a.ftable === value.table))){
                somethingDependsOnThis = true;
            }
        })
        if(somethingDependsOnThis){
            return "Valami hivatkozik erre a tablara";
        }
        else{

            var documentsArray = await client.db(value.database).collection(value.table).find().toArray();
            var array = [];
            var pathName = `./databases/${value.database}/${value.table}/${value.table}.json`;
            let cells = require(pathName);
            documentsArray.forEach(d => {
                let cellNames = value.order.split(",");
                let stringArray = d.values.split("#");
                let document = {};
                for(let i = 0; i < stringArray.length - 1; i++){
                    document[cellNames[i]] = stringArray[i];
                }

                document[getPrimaryKey(value)] = d.id;

                for(c of cells){
                    if(c.fk){
                        indexClient.db(value.database).collection(c.ftable + "." + c.fattr)
                            .updateOne({_id: document[c.name]}, {$inc : {references: -1}});
                    }
                }

            });
            
            for(c of cells){
                if(c.unique){
                    indexClient.db(value.database).dropCollection(value.table + "." + c.name);
                }
            }
            fs.rmSync(fname,  { recursive: true, force: true });
            client.db(value.database).dropCollection(value.table);
            return "OK";
        }
    }
    else{
        return "A tabla nem letezik.";
    }
}

function getAttributesByType(value){
    if(value.table === '' || value.table === null){
        return '';
    }
    var attributes = require(`./databases/${value.database}/${value.table}/${value.table}.json`);
    return attributes.filter(a => a.type === value.type && a.unique).map(a => a.name).toString();
}

function getTableValueNames(value){
    var pathName = `./databases/${value.database}/${value.table}/${value.table}.json`;
    if(fs.existsSync(pathName)){
        var valami = {};
        require(pathName).forEach(v => valami[v.name]= v.type);
        return JSON.stringify(valami);
    }
    else{
        return "Nem letezik a tabla";
    }
}

function getPrimaryKey(value){
    if(value.table == "" || value.table == null){
        return "";
    }
    var pathName = `./databases/${value.database}/${value.table}/${value.table}.json`;
    var cells = require(pathName);
    var primary;
    cells.forEach(c => {if(c.pk){primary = c.name}})
    return primary;
}

async function insertIntoTable(value) {
    var pathName = `./databases/${value.database}/${value.table}/${value.table}.json`;
    if (fs.existsSync(pathName)) {
        var cellTypes = {};
        //type verification
        require(pathName).forEach(c => cellTypes[c.name] = c.type);
        for (var ckey of Object.keys(value.cells)) {
            value.cells[ckey] = parameterToType(value.cells[ckey], cellTypes[ckey]);
            if (parameterToType(value.cells[ckey], cellTypes[ckey]) == undefined) {
                return `${ckey} fomatuma nem egyezik meg`;
            }
        }


        let primaryKey = getPrimaryKey(value);
        let otherValues = "";
        Object.keys(value.cells).forEach(ckey => {if(ckey != primaryKey) otherValues += value.cells[ckey] + "#"});
        


        cells = require(pathName);

        
        //unqiue check
        for(c of cells){
            if(c.unique){
                let exists = (await indexClient.db(value.database).collection(value.table  + "." + c.name)
                            .findOne({_id: value.cells[c.name]})) !== null;
                if(exists){
                    return `${c.name} unique s mar van belole egy.`;
                }
            }
        }

        //fkey check
        for(c of cells){
            if(c.fk){
                let exists = (await indexClient.db(value.database).collection(c.ftable + "." + c.fattr)
                        .findOne({_id: value.cells[c.name]})) !== null;
                if(!exists){
                    return `${c.name} hivatkozik mas tablara, de ott nincs jelen az ertek.`;
                }
            }
        }

        //inserting into table
        let toInsert = {_id: value.cells[primaryKey], values: otherValues};
        try{
            client.db(value.database).collection(value.table).insertOne(toInsert);
        }
        catch(e){
            console.log("1!!");
        }
        
        //inserting nito unique index files
        cells.forEach(c => {
            if(c.unique){
                try{
                    indexClient.db(value.database).collection(value.table  + "." + c.name)
                        .insertOne({_id: value.cells[c.name], pk: value.cells[primaryKey], references: 0});
                }
                catch(e){
                    console.log("2!!");
                }
            }
        });
        
        //inserting into not unique index files
        for(c of cells){
            if(c.index && !c.unique){
                try{
                    await indexClient.db(value.database).collection(value.table + "." + c.name)
                        .insertOne({_id: value.cells[c.name], pks: []});
                }
                catch(e){
                    console.log("4!!");
                }
                await indexClient.db(value.database).collection(value.table + "." + c.name)
                        .updateOne({_id: value.cells[c.name]}, {$push: {pks: value.cells[primaryKey]}})
            }
        }

        //incrementing references in foreign table
        for(c of cells){
            if(c.fk){
                indexClient.db(value.database).collection(c.ftable + "." + c.fattr)
                    .updateOne({_id: value.cells[c.name]}, {$inc : {references: 1}});
            }
        }
        return "OK";
    }
    else {
        return "Nem letezik a tabla :(.";
    }
}

function parameterToType(p, type){
    switch(type){
        case 'int':
            if(parseInt(p) == p){
                return parseInt(p);
            }
            else{
                return undefined;
            }
        case 'float':
            if(parseFloat(p) == p){
                return parseInt(p);
            }
            else{
                return undefined;
            }
        case 'string':
            if(p.toString() !== "" && !/#/.test(p)){
                return p.toString();
            }
            else{
                return undefined;
            }
        case 'date':
            if(/^\d{4}\/\d{2}\/\d{2}$/.test(p) && new Date(p) != 'Invalid Date'){
                return p;
            }
            else{
                return undefined;
            }
        case 'datetime':
            if(/^\d{4}\/\d{2}\/\d{2}:\d{2}:\d{2}$/.test(p) && new Date(p) != 'Invalid Date'){
                return p;
            }
            else{
                return undefined;
            }
        case 'bit':
            if(p === '0'){
                return 0;
            }
            else if(p === '1'){
                return 1;
            }
            else{
                return undefined;
            }
        default:
            return undefined;
    }
}


async function getDocumentsFromTable(value){
    if(value.table == "" || value.table == null){
        return [];
    }
    var documents = await client.db(value.database).collection(value.table).find().toArray();
    var array = [];
    documents.forEach(d => {
        array.push([]);
        array[array.length - 1].push(d._id);
        var values = d.values.split('#');
        array[array.length - 1] = array[array.length - 1].concat(values);
    });
    return array;
}

async function deleteDocumentsFromTable(value){

    let pathName = `./databases/${value.database}/${value.table}/${value.table}.json`;
    let cells = require(pathName);
    let cellTypes = {};

    cells.forEach(c => cellTypes[c.name] = c.type);
    let primaryKey;
    cells.forEach(c => {
        if(c.pk){
            primaryKey = c.name;
        }
    });

    let nemTorolheto = [];
    value.cells = value.cells.map(i => parameterToType(i, cellTypes[primaryKey]));
    for(id of value.cells){
        let document = {};
        let querry = await client.db(value.database).collection(value.table)
                    .findOne({_id : id});
        
        if(querry === null){
            //nem kaptuk meg a cuccot
            return "NEM LETEZIK:((((("
        }


        let i = 0;
        let stringArray = querry.values.split("#");
        let cellNames = value.order.split(",");

        for(let i = 0; i < stringArray.length - 1; i++){
            document[cellNames[i]] = parameterToType(stringArray[i], cellTypes[cellNames[i]]);
        }
        document[getPrimaryKey(value)] = id;

        let needsToBreak = false;
        for(c of cells){
            if(c.unique){
                let ref = (await indexClient.db(value.database).collection(value.table + "." + c.name)
                            .findOne({_id: document[c.name]})).references;
                if(ref){
                    nemTorolheto.push(id);
                    needsToBreak = true;
                    break;
                }
            }
        }

        if(needsToBreak){
            continue;
        }


        for(c of cells){
            if(c.fk){
                indexClient.db(value.database).collection(c.ftable + "." + c.fattr)
                    .updateOne({_id: document[c.name]}, {$inc : {references: -1}});
            }
        }

        for(c of cells){
            if(c.unique){
                indexClient.db(value.database).collection(value.table + "." + c.name)
                    .deleteOne({_id: document[c.name]});
            }
        }

        for(c of cells){
            if(!c.unique && c.index){
                indexClient.db(value.database).collection(value.table + "." + c.name)
                    .updateOne({_id: document[c.name]}, {$pull : {pks: id}});
            }
        }

        client.db(value.database).collection(value.table)
            .deleteOne({_id : id});
        // client.db(value.database).collection(value.table).deleteOne({_id: id}, (err, obj) =>{
        //     if(err){
        //         console.log("Egy bejegyzes nem letezett.:(");
        //     }
        // });
    }
    if(nemTorolheto.length){
        let s = "Toroltem amit tudtam, de nem toroltem: ";
        nemTorolheto.forEach(n => s += n + ", ");
        return s;
    }
    return "OK";
}


async function selectAndFilter(value){

    tables = {};
    typeOfCells = {};
    indexOfCells = {};
    uniqueOfCells = {};


    //get the tables used
    for(let f of value.filters){
        if(tables[f.table] === undefined){
            let pathName = `./databases/${value.database}/${f.table}/${f.table}.json`;
            tables[f.table] = require(pathName);
            typeOfCells[f.table] = {};
            indexOfCells[f.table] = {};
            uniqueOfCells[f.table] = {};
        }
    }

    for(let t of Object.keys(tables)){
        for(let c of tables[t]){
            typeOfCells[t][c.name] = c.type;
            indexOfCells[t][c.name] = c.index;
            uniqueOfCells[t][c.name] = c.unique;
        }
    }

    //check type for every filter
    for(let i of Object.keys(value.filters)){
        f = value.filters[i];
        value.filters[i].value = parameterToType(f.value, typeOfCells[f.table][f.field]);
        if(parameterToType(f.value, typeOfCells[f.table][f.field]) === undefined){
            return `HIBA ${value.filters.indexOf(f)}. feltetel tipusa hibas`;
        }
        if(typeOfCells[f.table][f.field] == 'string' || typeOfCells[f.table][f.field] == ''){
            return `HIBA ${value.filters.indexOf(f)}. feltetel tipusa string/date, az operatornem megengedett.`;
        }
    }

    let indexedFilters = [];
    let notIndexedFilters = [];
    //filtering by index the filters
    for(let f of value.filters){
        if(indexOfCells[f.table][f.field]){
            indexedFilters.push(f);
        }
        else{
            notIndexedFilters.push(f);
        }
    }
    
    let arrayOfPks = await client.db(value.database).collection(value.table)
                        .find().toArray();
    Object.keys(arrayOfPks).forEach(k => {
        arrayOfPks[k] = arrayOfPks[k]._id;
    });
    //console.log(arrayOfPks);

    //console.log(indexedFilters);
    for(f of indexedFilters){
        let newArrayOfPks;
        console.log(f.table + "." + f.field);
        console.log(value.database, f.value);
        switch(f.operator){
            case "<":
                newArrayOfPks = await indexClient.db(value.database).collection(f.table + "." + f.field)
                            .find({_id : { $lt: f.value}}).toArray();
                break;
            case ">":
                newArrayOfPks = await indexClient.db(value.database).collection(f.table + "." + f.field)
                            .find({_id : { $gt: f.value}}).toArray();
                break;
            case "=":
                newArrayOfPks = [(await indexClient.db(value.database).collection(f.table + "." + f.field)
                            .findOne({_id : f.value}))];
                            console.log("11111", newArrayOfPks);
                break;
            case "<=":
                newArrayOfPks = await indexClient.db(value.database).collection(f.table + "." + f.field)
                .find({_id : { $lte: f.value}}).toArray();
                break;
            case ">=":
                newArrayOfPks = await indexClient.db(value.database).collection(f.table + "." + f.field)
                            .find({_id : { $gte: f.value}}).toArray();
                break;
        }
        console.log(newArrayOfPks);

        Object.keys(newArrayOfPks).forEach(k => {
            if(uniqueOfCells[f.table][f.field]){
                newArrayOfPks[k] = newArrayOfPks[k].pk;
            }
            else{
                newArrayOfPks[k] = newArrayOfPks[k].pks;
            }
        });
        newArrayOfPks = newArrayOfPks.flat();

        arrayOfPks = arrayOfPks.filter(v => newArrayOfPks.includes(v));
    }
    console.log("1", arrayOfPks);
    
    let records = await client.db(value.database).collection(value.table)
                        .find({_id: {$in: arrayOfPks}}).toArray();

    let newRecords = [];
    for(let r of records){
        newRecords.push([r._id]);
        newRecords[newRecords.length - 1] = newRecords[newRecords.length - 1].concat(r.values.split('#'));
    }

    console.log(newRecords);
    for(f of notIndexedFilters){
        //why do we need to do this
        f.order = f.order.split(',');
        let point = 1 + f.order.indexOf(f.field);
        
        

        switch(f.operator){
            case "<":
                newRecords = newRecords.filter(a => a[point] < f.value);
                break;
            case ">":
                newRecords = newRecords.filter(a => a[point] > f.value);
                break;
            case "=":
                console.log(newRecords);
                newRecords = newRecords.filter(a => a[point] == f.value);
                break;
            case "<=":
                newRecords = newRecords.filter(a => a[point] <= f.value);
                break;
            case ">=":
                newRecords = newRecords.filter(a => a[point] >= f.value);
                break;
        }
    }
    console.log(newRecords);
    return newRecords;
}


const server = net.createServer((socket) => {

    socket.on('data', async (data) =>{
        data = JSON.parse(data.toString())
        console.log("I got a request: " + data.command);
        let answer;
        switch(data.command){
            case "Create Database":
                answer = createDatabase(data.value);
                break;
            case "Get Databases":
                answer = getDatabases();
                break;
            case "Drop Database":
                answer = dropDatabase(data.value);
                break;
            case "Create Table":
                answer = createTable(data.value);
                break;
            case "Get Tables":
                answer = getTables(data.value);
                break;
            case "Drop Table":
                answer = await dropTable(data.value);
                break;
            case "Get Attributes By Type":
                answer = getAttributesByType(data.value);
                break;
            case "Get Table Values":
                answer = getTableValueNames(data.value);
                break;
            case "Insert Into Table":
                answer = await insertIntoTable(data.value);
                break;
            case "Get Primary Key":
                answer = getPrimaryKey(data.value);
                break;
            case "Get Documents From Table":
                answer = await getDocumentsFromTable(data.value);
                break;
            case "Delete Documents From Table":
                answer = await deleteDocumentsFromTable(data.value);
                break;
            case "Select":
                answer = await selectAndFilter(data.value);
                break;        
        }
        //console.log(answer);
        socket.write(answer + '\n');
        socket.pipe(socket);
        socket.destroy();
    });
});


server.listen(port, async () =>{
    console.log(`Server is listening on http://localhost:${port}`)
    client.connect(err =>{
        console.log("hello Sima CLIENT");
        if(err){
            console.log(err)
        }
    });
    indexClient.connect(err =>{
        console.log("HELLO INDEXCLIENT");
        if(err){
            console.log(err)
        }
    });
})

server.on('close', () =>{
    client.close();
    indexClient.close();
});