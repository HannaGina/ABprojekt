const net = require('net')
const fs = require('fs');
const { MongoClient, ServerApiVersion } = require('mongodb');
const { join } = require('path');
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
            //csak akkor hajtsuk vegre, ha nem az egyeduli tabla
            if(value.order){
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
            }
            
            for(c of cells){
                if(c.index){
                    indexClient.db(value.database).dropCollection(value.table + "." + c.name, (err) => {});
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
            if (value.cells[ckey] == undefined) {
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

    //how to
    if(value.projections.length === 2 && value.filters.length === 0 && !value.dontDoIt && !value.groupBy.length){
        let projectionName = value.projections[1];
        let tname = projectionName.split('.')[0];
        let cname = projectionName.split('.')[1];
        let pathName = `./databases/${value.database}/${tname}/${tname}.json`;
        tables[tname] = require(pathName);
        indexOfCells[tname] = {};

        for(let c of tables[tname]){
            indexOfCells[tname][c.name] = c.index;
        }

        if (indexOfCells[tname][cname]){
            let array = await indexClient.db(value.database).collection(tname + "." + cname)
                            .find({}).toArray();
            if(array != null){
                console.log(array);
                array = array.map(a => a._id);
            }
            else{
                array = []
            }
            return {array: array, onlyIndex: 1};
        }
    }


    //check type for every filter
    for(let i of Object.keys(value.filters)){
        f = value.filters[i];
        value.filters[i].value = parameterToType(f.value, typeOfCells[f.table][f.field]);
        if(parameterToType(f.value, typeOfCells[f.table][f.field]) === undefined){
            return {array: `HIBA ${value.filters.indexOf(f)}. feltetel tipusa hibas`, onlyIndex: 0};
        }
        if( (typeOfCells[f.table][f.field] == 'string' || typeOfCells[f.table][f.field] == '' || typeOfCells[f.table][f.field] == 'date')
            && f.operator != "="){
            return {array: `HIBA ${value.filters.indexOf(f)}. feltetel tipusa string/date, az operatornem megengedett.`, onlyIndex: 0};
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

    //if there is an index, start by not getting all the values
    let arrayOfPks;
    if(indexedFilters.length > 0){
        const f = indexedFilters[0];
        switch(f.operator){
            case "<":
                arrayOfPks = await indexClient.db(value.database).collection(f.table + "." + f.field)
                            .find({_id : { $lt: f.value}}).toArray();
                break;
            case ">":
                arrayOfPks = await indexClient.db(value.database).collection(f.table + "." + f.field)
                            .find({_id : { $gt: f.value}}).toArray();
                break;
            case "=":
                arrayOfPks = [(await indexClient.db(value.database).collection(f.table + "." + f.field)
                            .findOne({_id : f.value}))];
                break;
            case "<=":
                arrayOfPks = await indexClient.db(value.database).collection(f.table + "." + f.field)
                .find({_id : { $lte: f.value}}).toArray();
                break;
            case ">=":
                arrayOfPks = await indexClient.db(value.database).collection(f.table + "." + f.field)
                            .find({_id : { $gte: f.value}}).toArray();
                break;
        }
    }
    else{
        arrayOfPks = await client.db(value.database).collection(value.table)
                            .find().toArray(); 
    }
    if(arrayOfPks[0] != null){
        console.log(arrayOfPks);
        Object.keys(arrayOfPks).forEach(k => {
            
            arrayOfPks[k] = arrayOfPks[k]._id;
        });
    }
    else {
        arrayOfPks = [];
    }

    for(f of indexedFilters){
        let newArrayOfPks;
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
        
        if(newArrayOfPks[0] != null){
            Object.keys(newArrayOfPks).forEach(k => {
                if(uniqueOfCells[f.table][f.field]){
                    newArrayOfPks[k] = newArrayOfPks[k].pk;
                }
                else{
                    newArrayOfPks[k] = newArrayOfPks[k].pks;
                }
            });
        }
        else{
            newArrayOfPks = [];
        }
        newArrayOfPks = newArrayOfPks.flat();

        arrayOfPks = arrayOfPks.filter(v => newArrayOfPks.includes(v));
    }
    
    let records = await client.db(value.database).collection(value.table)
                        .find({_id: {$in: arrayOfPks}}).toArray();

    let newRecords = [];
    for(let r of records){
        newRecords.push([r._id]);
        newRecords[newRecords.length - 1] = newRecords[newRecords.length - 1].concat(r.values.split('#'));
    }

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
    return {array: newRecords, onlyIndex: 0};
}

async function joinAndFilter (value) {
    let originalJoins = JSON.parse(JSON.stringify(value.joins));
    let tables = {};
    let typeOfCells = {};
    let indexOfCells = {};
    let uniqueOfCells = {};

    if(value.joins.length === 0){
        let rJson = await selectAndFilter(value);
        return rJson;
    }

    //hmmmmmmmmm
    //do the where for every stuff, and then make join table;
    let whereClauses = {};
    for(t of value.joinTables){
        whereClauses[t] = [];
    }

    for(f of value.filters){
        whereClauses[f.table].push(f);
    }

    for(t of value.joinTables){
        const pathName = `./databases/${value.database}/${t}/${t}.json`;
        tables[t] = require(pathName);
        typeOfCells[t] = {};
        indexOfCells[t] = {};
        uniqueOfCells[t] = {};
    }

    for(t of Object.keys(tables)){
        for(let c of tables[t]){
            typeOfCells[t][c.name] = c.type;
            indexOfCells[t][c.name] = c.index;
            uniqueOfCells[t][c.name] = c.unique;
        }
    }


    let resultsOfSelects = {};
    for(t of value.joinTables){
        let newValue = value;
        newValue.filters = whereClauses[t];
        newValue.dontDoIt = true;
        newValue.table = t;
        resultsOfSelects[t] = (await selectAndFilter(newValue)).array;
        console.log(resultsOfSelects[t]);
        for(let i =0; i < resultsOfSelects[t].length; i++){
            resultsOfSelects[t][i].splice(resultsOfSelects[t][i].length - 1, 1);
            
        }
    }
    

    //here comes the join magic
    let objectOfResults = {};
    for(t of Object.keys(tables)){
        objectOfResults[t] = {};
    }
    joinPks = [];

    //INITIALIZE BY FIRST JOIN
    let joinc = value.joins[0];
    value.joins.splice(0, 1);
    if(indexOfCells[joinc.table1][joinc.field1]){
        const orderTemp = joinc.order1, fieldTemp = joinc.field1, tableTemp = joinc.table1;
        joinc.order1 = joinc.order2;
        joinc.field1 = joinc.field2;
        joinc.table1 = joinc.table2;

        joinc.order2 = orderTemp;
        joinc.field2 = fieldTemp;
        joinc.table2 = tableTemp;
    }

    //parameter to good type
    let t1 = joinc.table1;
    for(let i = 0; i < resultsOfSelects[t1].length; i++){
        for(cell of joinc.order1){
            resultsOfSelects[t1][i][joinc.order1.indexOf(cell)] = parameterToType(resultsOfSelects[t1][i][joinc.order1.indexOf(cell)], typeOfCells[t1][cell]);
        }
        objectOfResults[t1][resultsOfSelects[t1][i][0]] = resultsOfSelects[t1][i]
    }

    let t2 = joinc.table2;
    for(let i = 0; i < resultsOfSelects[t2].length; i++){
        for(cell of joinc.order2){
            resultsOfSelects[t2][i][joinc.order2.indexOf(cell)] = parameterToType(resultsOfSelects[t2][i][joinc.order2.indexOf(cell)], typeOfCells[t2][cell]);
        }
        objectOfResults[t2][resultsOfSelects[t2][i][0]] = resultsOfSelects[t2][i];
    }
    
    //IF THERE IS AN INDEX
    if(indexOfCells[joinc.table2][joinc.field2]){

        for(let i = 0; i < resultsOfSelects[joinc.table1].length; i++){
            let val = resultsOfSelects[joinc.table1][i][joinc.order1.indexOf(joinc.field1)];
            
            let doc = await indexClient.db(value.database).collection(joinc.table2 + '.' + joinc.field2)
                        .findOne({_id: val});
            if(doc === null){
                continue;
            }
            
            

            if(doc.pk !== undefined){
                if(objectOfResults[joinc.table2][doc.pk] !== undefined){
                    let t1 = joinc.table1;
                    let t2 = joinc.table2;
                    let tempObj = {};
                    tempObj[t1] = resultsOfSelects[joinc.table1][i][0];
                    tempObj[t2] = doc.pk;
                    joinPks.push(tempObj);
                }
            }
            else{
                for(pk of doc.pks){
                    if(objectOfResults[joinc.table1][pk] !== undefined){
                        let t1 = joinc.table1;
                        let t2 = joinc.table2;
                        let tempObj = {};
                        tempObj[t1] = resultsOfSelects[joinc.table1][i][0];
                        tempObj[t2] = pk;
                        joinPks.push(tempObj);
                    }
                }
            }
        }
    }
    else {
        //double for if no index
        for(doc1 of Object.values(objectOfResults[joinc.table1])){
            for(doc2 of Object.values(objectOfResults[joinc.table2])){
                const val1 = doc1[joinc.order1.indexOf(joinc.field1)];
                const val2 = doc2[joinc.order2.indexOf(joinc.field2)];
                if(val1 == val2){
                    let t1 = joinc.table1;
                    let t2 = joinc.table2;
                    let tempObj = {};
                    tempObj[t1] = doc1[0];
                    tempObj[t2] = doc2[0];
                    joinPks.push(tempObj);
                }
            }
        }
    }

    //for the rest
    let joinPks2 = joinPks;
    for(joinc of value.joins){
        joinPks = joinPks2;
        joinPks2 = [];

        //parameter conversion
        let t1 = joinc.table1;
        for(let i = 0; i < resultsOfSelects[t1].length; i++){
            for(cell of joinc.order1){
                resultsOfSelects[t1][i][joinc.order1.indexOf(cell)] = parameterToType(resultsOfSelects[t1][i][joinc.order1.indexOf(cell)], typeOfCells[t1][cell]);
            }
            objectOfResults[t1][resultsOfSelects[t1][i][0]] = resultsOfSelects[t1][i]
        }

        let t2 = joinc.table2;
        for(let i = 0; i < resultsOfSelects[t2].length; i++){
            for(cell of joinc.order2){
                resultsOfSelects[t2][i][joinc.order2.indexOf(cell)] = parameterToType(resultsOfSelects[t2][i][joinc.order2.indexOf(cell)], typeOfCells[t2][cell]);
            }
            objectOfResults[t2][resultsOfSelects[t2][i][0]] = resultsOfSelects[t2][i];
        }

        if(indexOfCells[joinc.table1][joinc.field1]){
            for(let i = 0; i < joinPks.length; i++){
                const jp = joinPks[i];
                const pk2 = jp[joinc.table2];
                const val = objectOfResults[joinc.table2][pk2][joinc.order2.indexOf(joinc.field2)];

                const doc = await indexClient.db(value.database).collection(joinc.table1 + '.' + joinc.field1)
                    .findOne({_id: val});
                
                

                if(doc === null){
                    continue;
                }

                if(doc.pk !== undefined){
                    if(objectOfResults[joinc.table1][doc.pk] !== undefined){
                        let t1 = joinc.table1;
                        let tempObj = {};
                        Object.assign(tempObj, joinPks[i]);
                        tempObj[t1] = doc.pk;
                        
                        joinPks2.push(tempObj);
                    }
                }
                else{
                    for(pk of doc.pks){
                        if(objectOfResults[joinc.table1][pk] !== undefined){
                            let t1 = joinc.table1;
                            let tempObj = {};
                            Object.assign(tempObj, joinPks[i]);
                            tempObj[t1] = pk;
                            joinPks2.push(tempObj);
                        }
                    }
                }
            }
        }
        else{
            for(let i = 0; i < joinPks.length; i++){
                const jp = joinPks[i];
                const pk2 = jp[joinc.table2];
                const val = objectOfResults[joinc.table2][pk2][joinc.order2.indexOf(joinc.field2)];

                for(doc of Object.values(objectOfResults[joinc.table1])){
                    const val2 = doc[joinc.order1.indexOf(joinc.field1)];
                    if(val2 == val){
                        let t1 = joinc.table1;
                        let tempObj = {};
                        Object.assign(tempObj, joinPks[i]);
                        tempObj[t1] = doc[0];
                        joinPks2.push(tempObj);
                    }
                }
            }
        }
    }



    let result = [];
    for(joinPk of joinPks2){
        let tempArray = [];
        for(t of value.joinTables){
            tempArray = tempArray.concat(objectOfResults[t][joinPk[t]]);
        }
        result.push(tempArray);
    }
    value.joins = originalJoins;
    return {array: result, onlyIndex: '0'};
}

async function groupAndSelect(value){
    let tables = {};
    let typeOfCells = {};
    let indexOfCells = {};
    let uniqueOfCells = {};
    
    let selectResults = await joinAndFilter(value);
    if(value.groupBy.length === 0){
        if(selectResults.onlyIndex){
            for(arrayRowResult of selectResults.array){
                arrayRowResult.push('');
            }
            selectResults.array = selectResults.array.toString();
            console.log(1 ,selectResults);
        }

        let newArray = [];
        for(arrayRow of selectResults.array){
            newArray = newArray.concat(arrayRow);
        }
        selectResults.array = newArray.toString();
        return JSON.stringify(selectResults);
    }



    for(t of value.joinTables){
        const pathName = `./databases/${value.database}/${t}/${t}.json`;
        tables[t] = require(pathName);
        typeOfCells[t] = {};
        indexOfCells[t] = {};
        uniqueOfCells[t] = {};
    }

    for(t of Object.keys(tables)){
        for(let c of tables[t]){
            typeOfCells[t][c.name] = c.type;
            indexOfCells[t][c.name] = c.index;
            uniqueOfCells[t][c.name] = c.unique;
        }
    }
    value.groupByOrder = value.groupByOrder.substring(1);
    value.groupByOrder = value.groupByOrder.slice(0, -1);
    value.groupByOrder = value.groupByOrder.split(', ');
    //group stuff by group by


    //validate groupByProjection
    for(gProjection of value.groupByProjection){
        const type = typeOfCells[gProjection.table][gProjection.field];
        if(type === 'string' || type === 'date' || type === 'datetime'){
            if(gProjection.function === 'AVG' || gProjection.function === 'SUM'){
                return JSON.stringify({array: `HIBA ${gProjection.table}.${gProjection.field} tipusa ${type}, nem lehet ${gProjection.function}-t hasznalni ra.`,
                            onlyIndex: 0});
            }
        }
    }




    let groupByObj = {};
    for(arrayRow of selectResults.array){
        let groupByIdentifier = '';
        for(column of value.groupBy){
            let fullName = `${column.table}.${column.field}`;
            groupByIdentifier += arrayRow[value.groupByOrder.indexOf(fullName)].toString();
            groupByIdentifier += "#";
        }

        if(!groupByObj[groupByIdentifier]){
            groupByObj[groupByIdentifier] = [];
        }

        groupByObj[groupByIdentifier].push(arrayRow);
    }

    let resultArray = [];
    for(groupByArrays of Object.values(groupByObj)){
        let newArray = [];
        for(column of value.groupBy){
            let fullName = `${column.table}.${column.field}`;
            newArray.push(groupByArrays[0][value.groupByOrder.indexOf(fullName)]);
        }
        
        for(gProjection of value.groupByProjection){
            let newValue;
            let fullName = `${gProjection.table}.${gProjection.field}`;
            for(arrayRow of groupByArrays){  
                let currentValue = arrayRow[value.groupByOrder.indexOf(fullName)];
                switch(gProjection.function){
                    case 'MIN':
                        if(!newValue || currentValue < newValue){
                            newValue = currentValue;
                        }
                        break;
                    case 'MAX':
                        if(!newValue || currentValue > newValue){
                            newValue = currentValue;
                        }
                        break;
                    case 'AVG':
                        if(!newValue){
                            newValue = 0;
                        }
                        newValue += currentValue;
                        break;
                    case 'COUNT':
                        if(!newValue){
                            newValue = 0;
                        }
                        newValue++;
                        break;
                    case 'SUM':
                        if(!newValue){
                            newValue = 0;
                        }
                        newValue += currentValue;
                        break;
                }
            }

            if(gProjection.function === 'AVG'){
                newValue /= groupByArrays.length;
            }
            newArray.push(newValue);
        }

        newArray.push('');
        resultArray = resultArray.concat(newArray);
    }
    console.log(resultArray);
    return JSON.stringify({array: resultArray.toString(), onlyIndex: 0})
}


const server = net.createServer((socket) => {

    socket.on('data', async (data) =>{
        data = JSON.parse(data.toString());
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
                answer = await groupAndSelect(data.value);
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
});

server.on('close', () =>{
    client.close();
    indexClient.close();
});