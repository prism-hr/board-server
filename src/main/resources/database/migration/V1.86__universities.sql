SET @uclDocumentLogoId = (
  SELECT resource.document_logo_id
  FROM resource
  WHERE resource.scope = 'UNIVERSITY');

UPDATE resource
SET resource.homepage = 'https://www.ucl.ac.uk',
  resource.document_logo_id = NULL
WHERE resource.scope = 'UNIVERSITY';

DELETE
FROM document
WHERE document.id = @uclDocumentLogoId;

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Abbey College',
        'https://www.abbeycollege.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'ABI College',
        'https://www.abicollege.org.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City of Oxford College',
        'https://www.cityofoxford.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Banbury and Bicester College',
        'https://www.banbury-bicester.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Reading College',
        'https://www.reading-college.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Amsterdam Fashion Academy',
        'https://www.amsterdamfashionacademy.com', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bangor University',
        'https://www.bangor.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Barking and Dagenham College',
        'https://www.barkingdagenhamcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Barnet and Southgate College',
        'https://www.barnetsouthgate.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Barnfield College',
        'https://www.college.barnfield.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Barnsley College',
        'https://www.universitycampus.barnsley.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Basingstoke College of Technology',
        'https://www.bcot.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Berkshire College of Agriculture',
        'https://www.bca.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bexley College',
        'https://www.bexley.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bicton College',
        'https://www.bicton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'British and Irish Modern Music Institute',
        'https://www.bimm.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Birmingham Conservatoire',
        'https://www.bcu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bishop Burton College',
        'https://www.bishopburton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED',
        'Bishop Grosseteste University College Lincoln', 'https://www.bishopg.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Blackburn College',
        'https://www.blackburn.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Blackpool and The Fylde College',
        'https://www.blackpool.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bolton College',
        'https://www.boltoncollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bournemouth and Poole College',
        'https://www.thecollege.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bournemouth University',
        'https://www.bournemouth.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bournville College',
        'https://www.bournville.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'BPP University',
        'https://www.bpp.com', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bradford College',
        'https://www.bradfordcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bridgend College',
        'https://www.bridgend.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'British School of Osteopathy',
        'https://www.bso.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp) VALUES
  ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bromley College of Further and Higher Education',
   'https://www.bromley.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Brooksby Melton College',
        'https://www.brooksbymelton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Brunel University London',
        'https://www.brunel.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Buckinghamshire New University',
        'https://www.bucks.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Bury College',
        'https://www.burycollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Calderdale College',
        'https://www.calderdale.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Cambridge Regional College',
        'https://www.camre.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Cambridge School of Visual and Performing Arts',
        'https://www.csvpa.com', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Canterbury Christ Church University',
        'https://www.canterbury.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Canterbury College',
        'https://www.canterburycollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Cardiff Metropolitan University',
        'https://www.cardiffmet.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Cardiff University',
        'https://www.cardiff.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Carshalton College',
        'https://www.carshalton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Central Bedfordshire College',
        'https://www.centralbeds.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Central College Nottingham',
        'https://www.centralnottingham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Central Film School London',
        'https://www.centralfilmschool.com', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Chesterfield College',
        'https://www.chesterfield.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Chichester College',
        'https://www.chichester.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Christ the Redeemer College',
        'https://www.christredeemercollege.org', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City and Islington College',
        'https://www.candi.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City College Brighton and Hove',
        'https://www.ccb.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City College Coventry',
        'https://www.covcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City College Norwich',
        'https://www.ccn.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City College Plymouth',
        'https://www.cityplym.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City of Bristol College',
        'https://www.cityofbristol.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City of Glasgow College',
        'https://www.cityofglasgowcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City of London College',
        'https://www.clc-london.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City of Sunderland College',
        'https://www.sunderlandcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City of Westminster College',
        'https://www.cwc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City University London',
        'https://www.city.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Cleveland College of Art and Design',
        'https://www.ccad.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Cliff College',
        'https://www.cliffcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Colchester Institute',
        'https://www.colchester.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Coleg Llandrillo',
        'https://www.gllm.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Coleg Menai',
        'https://www.menai.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Coleg Sir Gar',
        'https://www.colegsirgar.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'College of West Anglia',
        'https://www.cwa.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Conservatoire for Dance and Drama',
        'https://www.cdd.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Cornwall College',
        'https://www.cornwall.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Courtauld Institute of Art',
        'https://www.courtauld.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Coventry University',
        'https://www.coventry.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Craven College',
        'https://www.craven-college.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Creative Academy',
        'https://www.creativeacademy.org', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'De Montfort University',
        'https://www.dmu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Dearne Valley College',
        'https://www.dearne-coll.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Derby College',
        'https://www.derby-college.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Doncaster College',
        'https://www.don.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Duchy College',
        'https://www.duchy.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Dudley College of Technology',
        'https://www.dudleycol.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Durham University',
        'https://www.durham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Ealing, Hammersmith and West London College',
        'https://www.wlc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'East Riding College',
        'https://www.eastridingcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'East Surrey College',
        'https://www.esc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Easton and Otley College',
        'https://www.eastonotley.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Edge Hill University',
        'https://www.edgehill.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Edge Hotel School',
        'https://www.edgehotelschool.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Edinburgh Napier University',
        'https://www.napier.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'European Business School, London',
        'https://www.ebslondon.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'European School of Economics',
        'https://www.eselondon.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'European School of Osteopathy',
        'https://www.eso.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Exeter College',
        'https://www.exe-coll.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Fareham College',
        'https://www.fareham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp) VALUES
  ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Furness College',
   'https://www.furnesscollege.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Gateshead College',
        'https://www.gateshead.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Glasgow Caledonian University',
        'https://www.gcu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Glasgow Kelvin College',
        'https://www.northglasgowcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Glasgow School of Art',
        'https://www.gsa.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Gloucestershire College',
        'https://www.gloscol.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Glyndwr University',
        'https://www.glyndwr.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Goldsmiths, University of London',
        'https://www.gold.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Gower College Swansea',
        'https://www.gowercollegeswansea.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Grŵp Colegau',
        'https://www.nptcgroup.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Greenwich School of Management (GSM London)',
        'https://www.gsm.org.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Guildford College',
        'https://www.guildford.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Guildhall School of Music and Drama',
        'https://www.gsmd.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Hackney Community College',
        'https://www.hackney.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Hadlow College',
        'https://www.hadlow.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Harper Adams University',
        'https://www.harper-adams.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Harrogate College',
        'https://www.harrogate.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Harrow College',
        'https://www.harrow.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Hartpury College',
        'https://www.hartpury.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Havering College of Further and Higher Education',
        'https://www.havering-college.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Heart of Worcestershire College',
        'https://www.howcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Henley College Coventry',
        'https://www.henley-cov.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Hereford College of Arts',
        'https://www.hca.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Heriot-Watt University',
        'https://www.hw.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Hertford Regional College',
        'https://www.hrc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Heythrop College',
        'https://www.heythrop.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Highbury College',
        'https://www.highbury.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Holy Cross Sixth Form College and University Centre',
        'https://www.holycross.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Hopwood Hall College',
        'https://www.hopwood.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp) VALUES
  ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Hugh Baird College',
   'https://www.hughbaird.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Hull College',
        'https://www.hull-college.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Hull York Medical School',
        'https://www.hyms.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Ifs University College',
        'https://www.ifslearning.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Imperial College London',
        'https://www.imperial.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Islamic College for Advanced Studies',
        'https://www.islamic-college.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Istituto Marangoni',
        'https://www.istitutomarangoni.com', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'John Ruskin College',
        'https://www.johnruskin.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Kaplan Holborn College',
        'https://www.holborncollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Keele University',
        'https://www.keele.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Kensington and Chelsea College',
        'https://www.kcc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Kensington College of Business',
        'https://www.kensingtoncoll.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Kings College London',
        'https://www.kcl.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Kingston College',
        'https://www.kingston-college.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Kingston Maurward College',
        'https://www.kmc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Kingston University',
        'https://www.kingston.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Kirklees College',
        'https://www.kirkleescollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'KLC School of Design',
        'https://www.klc.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Knowsley Community College',
        'https://www.knowsleycollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Lakes College - West Cumbria',
        'https://www.lcwc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Lancaster University',
        'https://www.lancaster.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'LCA Business School',
        'https://www.lca.anglia.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Leeds Beckett University',
        'https://www.leedsbeckett.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Leeds City College',
        'https://www.leedscitycollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Leeds College of Art',
        'https://www.leeds-art.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Leeds College of Building',
        'https://www.lcb.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Leeds College of Music',
        'https://www.lcm.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Leeds Trinity University',
        'https://www.leedstrinity.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Leicester College',
        'https://www.leicestercollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Lewisham Southwark College',
        'https://www.lesoco.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Lincoln College',
        'https://www.lincolncollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Liverpool Hope University',
        'https://www.hope.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Liverpool John Moores University',
        'https://www.ljmu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London Business School',
        'https://www.london.edu', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London Electronics College',
        'https://www.lec.org.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London Metropolitan University',
        'https://www.londonmet.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London School of Business and Management',
        'https://www.lsbm.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London School of Commerce',
        'https://www.lsclondon.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London School of Economics and Political Science',
        'https://www.lse.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London School of Hygiene and Tropical Medicine',
        'https://www.lshtm.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London School of Marketing',
        'https://www.londonschoolofmarketing.com', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London School of Science and Technology',
        'https://www.lsst.ac', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London South Bank University',
        'https://www.lsbu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Loughborough College',
        'https://www.loucoll.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Loughborough University',
        'https://www.lboro.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Macclesfield College',
        'https://www.macclesfield.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Medway School of Pharmacy',
        'https://www.msp.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Met Film School',
        'https://www.metfilmschool.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Metanoia Institute',
        'https://www.metanoia.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Mid Cheshire College',
        'https://www.midchesh.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Middlesex University',
        'https://www.mdx.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'MidKent College',
        'https://www.midkent.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Milton Keynes College',
        'https://www.mkcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Mont Rose College',
        'https://www.mrcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Moulton College',
        'https://www.moulton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Mountview Academy of Theatre Arts',
        'https://www.mountview.org.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Myerscough College',
        'https://www.myerscough.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Nazarene Theological College',
        'https://www.nazarene.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Nescot College',
        'https://www.nescot.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'New College Durham',
        'https://www.newcollegedurham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'New College Nottingham',
        'https://www.ncn.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'New College of the Humanities',
        'https://www.nchlondon.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'New College Stamford',
        'https://www.stamford.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'New College Telford',
        'https://www.nct.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Newcastle College',
        'https://www.newcastlecollege.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Newham College',
        'https://www.newham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Newman University',
        'https://www.newman.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Norland College',
        'https://www.norland.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'North Hertfordshire College',
        'https://www.nhc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'North Lindsey College',
        'https://www.northlindsey.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'North Warwickshire and Hinckley College',
        'https://www.nwhc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'North West Kent College',
        'https://www.northkent.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Northbrook College Sussex',
        'https://www.northbrook.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Northumberland College',
        'https://www.northumberland.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Northumbria University',
        'https://www.northumbria.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Norton Radstock College',
        'https://www.nortcoll.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Norwich University of the Arts',
        'https://www.nua.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Nottingham Trent University',
        'https://www.ntu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Oaklands College',
        'https://www.oaklands.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Oxford Brookes University',
        'https://www.brookes.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Park Royal College',
        'https://www.parkroyalcollege.org', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Pearson College',
        'https://www.pearsoncollege.com', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Pembrokeshire College',
        'https://www.pembrokeshire.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Peter Symonds College Adult and Higher Education',
        'https://www.psc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Petroc',
        'https://www.petroc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Plumpton College',
        'https://www.plumpton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Plymouth College of Art',
        'https://www.plymouthart.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Plymouth University',
        'https://www.plymouth.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Point Blank Music School',
        'https://www.pointblankonline.net', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Queen Margaret University, Edinburgh',
        'https://www.qmu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Queen Mary, University of London',
        'https://www.qmul.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Queens University Belfast',
        'https://www.qub.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Ravensbourne College of Design and Communication',
        'https://www.ravensbourne.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Reaseheath College',
        'https://www.reaseheath.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Regents University London',
        'https://www.regents.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Resource Development International',
        'https://www.rdi.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Richmond Upon Thames College',
        'https://www.rutc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Richmond, The American International University in London',
        'https://www.richmond.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Roehampton University',
        'https://www.roehampton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Rose Bruford College of Speech and Drama',
        'https://www.bruford.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Rotherham College of Arts and Technology',
        'https://www.rotherham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Royal Academy of Dance',
        'https://www.rad.org.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Royal Academy of Music',
        'https://www.ram.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Royal College of Music',
        'https://www.rcm.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Royal Conservatoire of Scotland',
        'https://www.rcs.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Royal Holloway College',
        'https://www.royalholloway.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Royal Northern College of Music',
        'https://www.rncm.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Royal Veterinary College',
        'https://www.rvc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Royal Welsh College of Music and Drama',
        'https://www.rwcmd.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Coleg Brenhinol Cerdd a Drama Cymru',
        'https://www.rwcmd.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Runshaw Adult College',
        'https://www.runshaw.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Ruskin College Oxford',
        'https://www.ruskin.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'SAE Institute Oxford',
        'https://www.uk.sae.edu', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Salford City College',
        'https://www.salfordcc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Sandwell College',
        'https://www.sandwell.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Selby College',
        'https://www.selby.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Sheffield College',
        'https://www.sheffcol.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Sheffield Hallam University',
        'https://www.shu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'SOAS, University of London',
        'https://www.soas.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Solihull College',
        'https://www.solihull.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Somerset College',
        'https://www.somerset.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'South and City College Birmingham',
        'https://www.sccb.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'South Cheshire College',
        'https://www.s-cheshire.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'South Devon College',
        'https://www.southdevon.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'South Downs College',
        'https://www.southdowns.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'South Essex College',
        'https://www.southessex.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'South Gloucestershire and Stroud College',
        'https://www.sgscol.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'South Leicestershire College',
        'https://www.slcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'South Thames College',
        'https://www.south-thames.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'South Tyneside College',
        'https://www.stc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Southampton Solent University',
        'https://www.solent.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Southport College',
        'https://www.southport.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Sparsholt College Hampshire',
        'https://www.sparsholt.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Spurgeons College',
        'https://www.spurgeons.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'SRUC - Scotlands Rural College',
        'https://www.sruc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'St Georges, University of London',
        'https://www.sgul.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'St Helens College',
        'https://www.sthelens.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'St Marys College, Blackburn',
        'https://www.stmarysblackburn.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'St Marys University, Twickenham, London',
        'https://www.stmarys.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'St Patricks College, London',
        'https://www.st-patricks.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Staffordshire University',
        'https://www.staffs.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Stephenson College Coalville',
        'https://www.stephensoncoll.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Stockport College',
        'https://www.stockport.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Stourbridge College',
        'https://www.stourbridge.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Stranmillis University College',
        'https://www.stran.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Stratford upon Avon College',
        'https://www.stratford.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Sussex Coast College',
        'https://www.sussexcoast.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp) VALUES
  ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Sussex Downs College',
   'https://www.sussexdowns.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Swansea University',
        'https://www.swansea.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Swindon College',
        'https://www.swindon.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Tameside College',
        'https://www.tameside.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Teesside University',
        'https://www.tees.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'City of Liverpool College',
        'https://www.liv-coll.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'College of Estate Management',
        'https://www.cem.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'College of Haringey',
        'https://www.conel.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Institute of Cancer Research',
        'https://www.icr.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Institute of Contemporary Music Performance',
        'https://www.icmp.co.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Liverpool Institute for Performing Arts',
        'https://www.lipa.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'London College',
        'https://www.lcuck.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Manchester College',
        'https://www.themanchestercollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Manchester Metropolitan University',
        'https://www.mmu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Robert Gordon University',
        'https://www.rgu.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Royal Agricultural University',
        'https://www.rau.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Birmingham',
        'https://www.birmingham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Edinburgh',
        'https://www.ed.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Essex',
        'https://www.essex.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Manchester',
        'https://www.manchester.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Salford',
        'https://www.salford.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Strathclyde',
        'https://www.strath.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of West London',
        'https://www.uwl.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of York',
        'https://www.york.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Tottenham Hotspur Foundation',
        'https://www.tottenhamhotspur.com', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Tresham College of Further and Higher Education',
        'https://www.tresham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Trinity Laban Conservatoire of Music and Dance',
        'https://www.trinitylaban.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Truro and Penwith College',
        'https://www.truro-penwith.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Tyne Metropolitan College',
        'https://www.tynemet.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'UCL Institute of Education',
        'https://www.ioe.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Ulster University',
        'https://www.ulster.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University Campus Oldham',
        'https://www.uco.oldham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University Campus Scarborough',
        'https://www.coventry.ac.uk/', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University Campus Suffolk',
        'https://www.ucs.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University Centre Farnborough',
        'https://www.farn-ct.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University Centre Grimsby',
        'https://www.grimsby.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University Centre Peterborough',
        'https://www.ucp.ac.uk/', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University Centre, Croydon',
        'https://www.croydon.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University College Birmingham',
        'https://www.ucb.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University for the Creative Arts',
        'https://www.ucreative.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Bath',
        'https://www.bath.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Bolton',
        'https://www.bolton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Bradford',
        'https://www.bradford.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Bristol',
        'https://www.bristol.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Buckingham',
        'https://www.buckingham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Cambridge',
        'https://www.study.cam.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Central Lancashire',
        'https://www.uclan.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Chichester',
        'https://www.chi.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Cumbria',
        'https://www.cumbria.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Derby',
        'https://www.derby.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Dundee',
        'https://www.dundee.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of East Anglia',
        'https://www.uea.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of East London',
        'https://www.uel.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Exeter',
        'https://www.exeter.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Glasgow',
        'https://www.glasgow.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Gloucestershire',
        'https://www.glos.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Greenwich',
        'https://www.gre.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Hertfordshire',
        'https://www.herts.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Huddersfield',
        'https://www.hud.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Hull',
        'https://www.hull.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Kent',
        'https://www.kent.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Law',
        'https://www.law.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Leeds',
        'https://www.leeds.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Leicester',
        'https://www.le.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Lincoln',
        'https://www.lincoln.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Liverpool',
        'https://www.liv.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of London Institute in Paris',
        'https://www.ulip.london.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Newcastle',
        'https://www.ncl.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Northampton',
        'https://www.northampton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Nottingham',
        'https://www.nottingham.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Oxford',
        'https://www.ox.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Portsmouth',
        'https://www.port.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Reading',
        'https://www.reading.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Sheffield',
        'https://www.sheffield.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of South Wales',
        'https://www.southwales.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Southampton',
        'https://www.southampton.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of St Andrews',
        'https://www.st-andrews.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of St Mark and St John',
        'https://www.marjon.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Stirling',
        'https://www.stir.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Sunderland',
        'https://www.sunderland.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Surrey',
        'https://www.surrey.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Sussex',
        'https://www.sussex.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of the Arts, London',
        'https://www.arts.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of the Highlands and Islands',
        'https://www.uhi.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of the West of Scotland',
        'https://www.uws.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Wales Trinity Saint David',
        'https://www.uwtsd.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Warwick',
        'https://www.warwick.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Westminster',
        'https://www.westminster.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Wolverhampton',
        'https://www.wlv.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'University of Worcester',
        'https://www.worcester.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Uxbridge College',
        'https://www.uxbridgecollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Wakefield College',
        'https://www.wakefield.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Walsall College',
        'https://www.walsallcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Warrington Collegiate',
        'https://www.warrington.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Warwickshire College',
        'https://www.warwickshire.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'West Cheshire College',
        'https://www.west-cheshire.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'West Herts College',
        'https://www.westherts.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'West Kent and Ashford College',
        'https://www.westkent.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'West Lancashire College',
        'https://www.westlancs.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'West Thames College',
        'https://www.west-thames.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Westminster Kingsway College',
        'https://www.westking.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Weston College',
        'https://www.weston.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Weymouth College',
        'https://www.weymouth.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Wigan and Leigh College',
        'https://www.wigan-leigh.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Wiltshire College',
        'https://www.wiltshire.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Wirral Metropolitan College',
        'https://www.wmc.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Worcester College of Technology',
        'https://www.wortech.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Writtle College',
        'https://www.writtle.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Yeovil College',
        'https://www.ucy.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'York College',
        'https://www.yorkcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

INSERT INTO resource (scope, state, previous_state, name, homepage, created_timestamp, updated_timestamp)
VALUES ('UNIVERSITY', 'ACCEPTED', 'ACCEPTED', 'Yorkshire Coast College',
        'https://www.yorkshirecoastcollege.ac.uk', '2017-11-13 12:27:31', '2017-11-13 12:27:31');

UPDATE resource
SET resource.parent_id = resource.id
WHERE resource.scope = 'UNIVERSITY';

INSERT IGNORE INTO resource_relation (resource1_id, resource2_id, created_timestamp, updated_timestamp)
  SELECT
    resource.id,
    resource.id,
    '2017-11-13 12:27:31',
    '2017-11-13 12:27:31'
  FROM resource
  WHERE resource.scope = 'UNIVERSITY';

UPDATE resource
SET resource.homepage = 'https://www.ucl.ac.uk',
  resource.handle = 'ucl',
  resource.index_data = 'U516 C420 L535',
  resource.quarter = 20174,
  resource.summary = 'university/sfjj7z4s6vfc0qx4hlje',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611440/university/sfjj7z4s6vfc0qx4hlje.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University College London';

UPDATE resource
SET resource.homepage = 'https://www.abbeycollege.co.uk',
  resource.handle = 'abbey-college',
  resource.index_data = 'A100 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ydy1cl48xqp7gsem0gsx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611441/university/ydy1cl48xqp7gsem0gsx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Abbey College';

UPDATE resource
SET resource.homepage = 'https://www.abicollege.org.uk',
  resource.handle = 'abi-college',
  resource.index_data = 'A100 C420',
  resource.quarter = 20174,
  resource.summary = 'university/xs1mxtyvhgwuzvrabipw',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611443/university/xs1mxtyvhgwuzvrabipw.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'ABI College';

UPDATE resource
SET resource.homepage = 'https://www.cityofoxford.ac.uk',
  resource.handle = 'city-of-oxford-college',
  resource.index_data = 'C300 O100 O216 C420',
  resource.quarter = 20174,
  resource.summary = 'university/unaof5lfrihfv97y4zvs',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611444/university/unaof5lfrihfv97y4zvs.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City of Oxford College';

UPDATE resource
SET resource.homepage = 'https://www.banbury-bicester.ac.uk',
  resource.handle = 'banbury-and-bicester',
  resource.index_data = 'B516 A530 B223 C420',
  resource.quarter = 20174,
  resource.summary = 'university/akenhz11sk9xhswwn5lx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611445/university/akenhz11sk9xhswwn5lx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Banbury and Bicester College';

UPDATE resource
SET resource.homepage = 'https://www.reading-college.ac.uk',
  resource.handle = 'reading-college',
  resource.index_data = 'R352 C420',
  resource.quarter = 20174,
  resource.summary = 'university/snbpzw0sldtqsftnobdc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611446/university/snbpzw0sldtqsftnobdc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Reading College';

UPDATE resource
SET resource.homepage = 'https://www.amsterdamfashionacademy.com',
  resource.handle = 'amsterdam-fashion-academy',
  resource.index_data = 'A523 F250 A235',
  resource.quarter = 20174,
  resource.summary = 'university/jnt7zykzj2siqqy25jgy',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611447/university/jnt7zykzj2siqqy25jgy.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Amsterdam Fashion Academy';

UPDATE resource
SET resource.homepage = 'https://www.bangor.ac.uk',
  resource.handle = 'bangor-university',
  resource.index_data = 'B526 U516',
  resource.quarter = 20174,
  resource.summary = 'university/vhxpdbwtrypd9rack6ie',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611448/university/vhxpdbwtrypd9rack6ie.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bangor University';

UPDATE resource
SET resource.homepage = 'https://www.barkingdagenhamcollege.ac.uk',
  resource.handle = 'barking-and-dagenham',
  resource.index_data = 'B625 A530 D255 C420',
  resource.quarter = 20174,
  resource.summary = 'university/xevizjjfttxv3ktckete',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611449/university/xevizjjfttxv3ktckete.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Barking and Dagenham College';

UPDATE resource
SET resource.homepage = 'https://www.barnetsouthgate.ac.uk',
  resource.handle = 'barnet-and-southgate',
  resource.index_data = 'B653 A530 S323 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ohzbd6s3rzmrqzga3pae',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611451/university/ohzbd6s3rzmrqzga3pae.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Barnet and Southgate College';

UPDATE resource
SET resource.homepage = 'https://www.college.barnfield.ac.uk',
  resource.handle = 'barnfield-college',
  resource.index_data = 'B651 C420',
  resource.quarter = 20174,
  resource.summary = 'university/e41ivqwtpw7frulqawvu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611452/university/e41ivqwtpw7frulqawvu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Barnfield College';

UPDATE resource
SET resource.homepage = 'https://www.universitycampus.barnsley.ac.uk',
  resource.handle = 'barnsley-college',
  resource.index_data = 'B652 C420',
  resource.quarter = 20174,
  resource.summary = 'university/pizyi9aet8e8nwttjjxn',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611453/university/pizyi9aet8e8nwttjjxn.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Barnsley College';

UPDATE resource
SET resource.homepage = 'https://www.bcot.ac.uk',
  resource.handle = 'basingstoke-college-of',
  resource.index_data = 'B252 C420 O100 T254',
  resource.quarter = 20174,
  resource.summary = 'university/lx9ksmockk8qdqjpve94',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611454/university/lx9ksmockk8qdqjpve94.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Basingstoke College of Technology';

UPDATE resource
SET resource.homepage = 'https://www.bca.ac.uk',
  resource.handle = 'berkshire-college-of',
  resource.index_data = 'B626 C420 O100 A262',
  resource.quarter = 20174,
  resource.summary = 'university/k8cnvolo9xa7pqza5ftd',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611455/university/k8cnvolo9xa7pqza5ftd.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Berkshire College of Agriculture';

UPDATE resource
SET resource.homepage = 'https://www.bexley.ac.uk',
  resource.handle = 'bexley-college',
  resource.index_data = 'B240 C420',
  resource.quarter = 20174,
  resource.summary = 'university/agkeshpqkbnvnhqzmizq',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611457/university/agkeshpqkbnvnhqzmizq.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bexley College';

UPDATE resource
SET resource.homepage = 'https://www.bicton.ac.uk',
  resource.handle = 'bicton-college',
  resource.index_data = 'B235 C420',
  resource.quarter = 20174,
  resource.summary = 'university/carpnfx0zn8dnpcxes0k',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611458/university/carpnfx0zn8dnpcxes0k.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bicton College';

UPDATE resource
SET resource.homepage = 'https://www.bimm.co.uk',
  resource.handle = 'british-and-irish-modern',
  resource.index_data = 'B632 A530 I620 M365 M220 I523',
  resource.quarter = 20174,
  resource.summary = 'university/x6bawuvvjighsqibmvqg',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611460/university/x6bawuvvjighsqibmvqg.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'British and Irish Modern Music Institute';

UPDATE resource
SET resource.homepage = 'https://www.bcu.ac.uk',
  resource.handle = 'birmingham-conservatoire',
  resource.index_data = 'B655 C526',
  resource.quarter = 20174,
  resource.summary = 'university/mrtsh3f9zbcr2vikbejs',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611461/university/mrtsh3f9zbcr2vikbejs.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Birmingham Conservatoire';

UPDATE resource
SET resource.homepage = 'https://www.bishopburton.ac.uk',
  resource.handle = 'bishop-burton-college',
  resource.index_data = 'B210 B635 C420',
  resource.quarter = 20174,
  resource.summary = 'university/v4rlyhso0mnm2eeqsipr',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611463/university/v4rlyhso0mnm2eeqsipr.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bishop Burton College';

UPDATE resource
SET resource.homepage = 'https://www.bishopg.ac.uk',
  resource.handle = 'bishop-grosseteste',
  resource.index_data = 'B210 G623 U516 C420 L524',
  resource.quarter = 20174,
  resource.summary = 'university/aksvovnzsty2rigvahzu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611464/university/aksvovnzsty2rigvahzu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bishop Grosseteste University College Lincoln';

UPDATE resource
SET resource.homepage = 'https://www.blackburn.ac.uk',
  resource.handle = 'blackburn-college',
  resource.index_data = 'B421 C420',
  resource.quarter = 20174,
  resource.summary = 'university/mecwglbwgiz0acyyio44',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611466/university/mecwglbwgiz0acyyio44.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Blackburn College';

UPDATE resource
SET resource.homepage = 'https://www.blackpool.ac.uk',
  resource.handle = 'blackpool-and-the-fylde',
  resource.index_data = 'B421 A530 T000 F430 C420',
  resource.quarter = 20174,
  resource.summary = 'university/d5dwp0anic2afohheemv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611467/university/d5dwp0anic2afohheemv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Blackpool and The Fylde College';

UPDATE resource
SET resource.homepage = 'https://www.boltoncollege.ac.uk',
  resource.handle = 'bolton-college',
  resource.index_data = 'B435 C420',
  resource.quarter = 20174,
  resource.summary = 'university/lub9rtqsxlfow0virorx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611468/university/lub9rtqsxlfow0virorx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bolton College';

UPDATE resource
SET resource.homepage = 'https://www.thecollege.co.uk',
  resource.handle = 'bournemouth-and-poole',
  resource.index_data = 'B655 A530 P400 C420',
  resource.quarter = 20174,
  resource.summary = 'university/lpj0ntnfib3hewjixxap',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611469/university/lpj0ntnfib3hewjixxap.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bournemouth and Poole College';

UPDATE resource
SET resource.homepage = 'https://www.bournemouth.ac.uk',
  resource.handle = 'bournemouth-university',
  resource.index_data = 'B655 U516',
  resource.quarter = 20174,
  resource.summary = 'university/nmmvcn8x6oeyci1bss7c',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611470/university/nmmvcn8x6oeyci1bss7c.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bournemouth University';

UPDATE resource
SET resource.homepage = 'https://www.bournville.ac.uk',
  resource.handle = 'bournville-college',
  resource.index_data = 'B651 C420',
  resource.quarter = 20174,
  resource.summary = 'university/qp4rtx8msxsxthig6det',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611472/university/qp4rtx8msxsxthig6det.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bournville College';

UPDATE resource
SET resource.homepage = 'https://www.bpp.com',
  resource.handle = 'bpp-university',
  resource.index_data = 'B000 U516',
  resource.quarter = 20174,
  resource.summary = 'university/c64rg0svmtpsrradjhdz',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611473/university/c64rg0svmtpsrradjhdz.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'BPP University';

UPDATE resource
SET resource.homepage = 'https://www.bradfordcollege.ac.uk',
  resource.handle = 'bradford-college',
  resource.index_data = 'B631 C420',
  resource.quarter = 20174,
  resource.summary = 'university/d7kmf2mzfmspucg22xal',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611474/university/d7kmf2mzfmspucg22xal.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bradford College';

UPDATE resource
SET resource.homepage = 'https://www.bridgend.ac.uk',
  resource.handle = 'bridgend-college',
  resource.index_data = 'B632 C420',
  resource.quarter = 20174,
  resource.summary = 'university/gjqjn0vzealz3booeugr',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611475/university/gjqjn0vzealz3booeugr.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bridgend College';

UPDATE resource
SET resource.homepage = 'https://www.bso.ac.uk',
  resource.handle = 'british-school-of',
  resource.index_data = 'B632 S400 O100 O231',
  resource.quarter = 20174,
  resource.summary = 'university/jd3ghgtufxn8z0opiqed',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611477/university/jd3ghgtufxn8z0opiqed.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'British School of Osteopathy';

UPDATE resource
SET resource.homepage = 'https://www.bromley.ac.uk',
  resource.handle = 'bromley-college-of',
  resource.index_data = 'B654 C420 O100 F636 A530 H260 E323',
  resource.quarter = 20174,
  resource.summary = 'university/bfoc3uf2psg2ntiuq2hs',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611478/university/bfoc3uf2psg2ntiuq2hs.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bromley College of Further and Higher Education';

UPDATE resource
SET resource.homepage = 'https://www.brooksbymelton.ac.uk',
  resource.handle = 'brooksby-melton-college',
  resource.index_data = 'B621 M435 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ay4znikbibz6cllped6u',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611480/university/ay4znikbibz6cllped6u.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Brooksby Melton College';

UPDATE resource
SET resource.homepage = 'https://www.brunel.ac.uk',
  resource.handle = 'brunel-university-london',
  resource.index_data = 'B654 U516 L535',
  resource.quarter = 20174,
  resource.summary = 'university/vllbaqfvpvha4s2u1hpr',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611481/university/vllbaqfvpvha4s2u1hpr.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Brunel University London';

UPDATE resource
SET resource.homepage = 'https://www.burycollege.ac.uk',
  resource.handle = 'bury-college',
  resource.index_data = 'B600 C420',
  resource.quarter = 20174,
  resource.summary = 'university/mf0cacpq1j22tfvbz4wi',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611483/university/mf0cacpq1j22tfvbz4wi.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Bury College';

UPDATE resource
SET resource.homepage = 'https://www.calderdale.ac.uk',
  resource.handle = 'calderdale-college',
  resource.index_data = 'C436 C420',
  resource.quarter = 20174,
  resource.summary = 'university/kc08kd6v5pin5hzhmxyi',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611484/university/kc08kd6v5pin5hzhmxyi.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Calderdale College';

UPDATE resource
SET resource.homepage = 'https://www.camre.ac.uk',
  resource.handle = 'cambridge-regional',
  resource.index_data = 'C516 R254 C420',
  resource.quarter = 20174,
  resource.summary = 'university/wp711i2nxydi7uj9skbu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611486/university/wp711i2nxydi7uj9skbu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Cambridge Regional College';

UPDATE resource
SET resource.homepage = 'https://www.csvpa.com',
  resource.handle = 'cambridge-school-of',
  resource.index_data = 'C516 S400 O100 V240 A530 P616 A632',
  resource.quarter = 20174,
  resource.summary = 'university/fntmozbji5kh3jakxcac',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611488/university/fntmozbji5kh3jakxcac.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Cambridge School of Visual and Performing Arts';

UPDATE resource
SET resource.homepage = 'https://www.canterbury.ac.uk',
  resource.handle = 'canterbury-christ-church',
  resource.index_data = 'C536 C623 C620 U516',
  resource.quarter = 20174,
  resource.summary = 'university/zo2ugvc59sl5svq5y8gj',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611489/university/zo2ugvc59sl5svq5y8gj.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Canterbury Christ Church University';

UPDATE resource
SET resource.homepage = 'https://www.canterburycollege.ac.uk',
  resource.handle = 'canterbury-college',
  resource.index_data = 'C536 C420',
  resource.quarter = 20174,
  resource.summary = 'university/k7kxvsxyeorapnx4nqsx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611491/university/k7kxvsxyeorapnx4nqsx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Canterbury College';

UPDATE resource
SET resource.homepage = 'https://www.cardiffmet.ac.uk',
  resource.handle = 'cardiff-metropolitan',
  resource.index_data = 'C631 M361 U516',
  resource.quarter = 20174,
  resource.summary = 'university/qgynv6qniybwummo05ia',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611492/university/qgynv6qniybwummo05ia.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Cardiff Metropolitan University';

UPDATE resource
SET resource.homepage = 'https://www.cardiff.ac.uk',
  resource.handle = 'cardiff-university',
  resource.index_data = 'C631 U516',
  resource.quarter = 20174,
  resource.summary = 'university/vyixcagsak6hwiwmpyhm',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611493/university/vyixcagsak6hwiwmpyhm.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Cardiff University';

UPDATE resource
SET resource.homepage = 'https://www.carshalton.ac.uk',
  resource.handle = 'carshalton-college',
  resource.index_data = 'C624 C420',
  resource.quarter = 20174,
  resource.summary = 'university/uqfoh5zfvzqs5ijfruim',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611494/university/uqfoh5zfvzqs5ijfruim.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Carshalton College';

UPDATE resource
SET resource.homepage = 'https://www.centralbeds.ac.uk',
  resource.handle = 'central-bedfordshire',
  resource.index_data = 'C536 B316 C420',
  resource.quarter = 20174,
  resource.summary = 'university/bg6mqljwp9koeaywdhzn',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611495/university/bg6mqljwp9koeaywdhzn.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Central Bedfordshire College';

UPDATE resource
SET resource.homepage = 'https://www.centralnottingham.ac.uk',
  resource.handle = 'central-college',
  resource.index_data = 'C536 C420 N352',
  resource.quarter = 20174,
  resource.summary = 'university/qss1cujmmle9cbt3fu5r',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611496/university/qss1cujmmle9cbt3fu5r.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Central College Nottingham';

UPDATE resource
SET resource.homepage = 'https://www.centralfilmschool.com',
  resource.handle = 'central-film-school',
  resource.index_data = 'C536 F450 S400 L535',
  resource.quarter = 20174,
  resource.summary = 'university/entg9e63tr2swikhufep',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611498/university/entg9e63tr2swikhufep.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Central Film School London';

UPDATE resource
SET resource.homepage = 'https://www.chesterfield.ac.uk',
  resource.handle = 'chesterfield-college',
  resource.index_data = 'C236 C420',
  resource.quarter = 20174,
  resource.summary = 'university/h1u61njtlpd689sf5049',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611500/university/h1u61njtlpd689sf5049.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Chesterfield College';

UPDATE resource
SET resource.homepage = 'https://www.christredeemercollege.org',
  resource.handle = 'christ-the-redeemer',
  resource.index_data = 'C623 T000 R356 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ypgzalvk8u363mjmfalm',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611502/university/ypgzalvk8u363mjmfalm.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Christ the Redeemer College';

UPDATE resource
SET resource.homepage = 'https://www.candi.ac.uk',
  resource.handle = 'city-and-islington',
  resource.index_data = 'C300 A530 I245 C420',
  resource.quarter = 20174,
  resource.summary = 'university/p4cmdeebhmh6za2wk7gj',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611504/university/p4cmdeebhmh6za2wk7gj.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City and Islington College';

UPDATE resource
SET resource.homepage = 'https://www.covcollege.ac.uk',
  resource.handle = 'city-college-coventry',
  resource.index_data = 'C300 C420 C153',
  resource.quarter = 20174,
  resource.summary = 'university/h31ufoc2gsk5uozrfz3q',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611506/university/h31ufoc2gsk5uozrfz3q.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City College Coventry';

UPDATE resource
SET resource.homepage = 'https://www.ccn.ac.uk',
  resource.handle = 'city-college-norwich',
  resource.index_data = 'C300 C420 N620',
  resource.quarter = 20174,
  resource.summary = 'university/r9y15sklxvynfiizxugf',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611507/university/r9y15sklxvynfiizxugf.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City College Norwich';

UPDATE resource
SET resource.homepage = 'https://www.cityplym.ac.uk',
  resource.handle = 'city-college-plymouth',
  resource.index_data = 'C300 C420 P453',
  resource.quarter = 20174,
  resource.summary = 'university/jamfxpg1nkyqwwrmsv7o',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611509/university/jamfxpg1nkyqwwrmsv7o.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City College Plymouth';

UPDATE resource
SET resource.homepage = 'https://www.cityofbristol.ac.uk',
  resource.handle = 'city-of-bristol-college',
  resource.index_data = 'C300 O100 B623 C420',
  resource.quarter = 20174,
  resource.summary = 'university/p1n8tb1vcz9e6dedv1ib',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611510/university/p1n8tb1vcz9e6dedv1ib.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City of Bristol College';

UPDATE resource
SET resource.homepage = 'https://www.cityofglasgowcollege.ac.uk',
  resource.handle = 'city-of-glasgow-college',
  resource.index_data = 'C300 O100 G420 C420',
  resource.quarter = 20174,
  resource.summary = 'university/dlqz2m0xbzjkyuajpzil',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611512/university/dlqz2m0xbzjkyuajpzil.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City of Glasgow College';

UPDATE resource
SET resource.homepage = 'https://www.clc-london.ac.uk',
  resource.handle = 'city-of-london-college',
  resource.index_data = 'C300 O100 L535 C420',
  resource.quarter = 20174,
  resource.summary = 'university/fzr0uli78vcb8h0yqofr',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611514/university/fzr0uli78vcb8h0yqofr.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City of London College';

UPDATE resource
SET resource.homepage = 'https://www.sunderlandcollege.ac.uk',
  resource.handle = 'city-of-sunderland',
  resource.index_data = 'C300 O100 S536 C420',
  resource.quarter = 20174,
  resource.summary = 'university/rdu8ycgtqd2r4qc4sbpn',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611515/university/rdu8ycgtqd2r4qc4sbpn.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City of Sunderland College';

UPDATE resource
SET resource.homepage = 'https://www.cwc.ac.uk',
  resource.handle = 'city-of-westminster',
  resource.index_data = 'C300 O100 W235 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ffub5acmx2ld1wc55bh5',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611517/university/ffub5acmx2ld1wc55bh5.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City of Westminster College';

UPDATE resource
SET resource.homepage = 'https://www.city.ac.uk',
  resource.handle = 'city-university-london',
  resource.index_data = 'C300 U516 L535',
  resource.quarter = 20174,
  resource.summary = 'university/smfpr6rit4vfbihjugks',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611518/university/smfpr6rit4vfbihjugks.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City University London';

UPDATE resource
SET resource.homepage = 'https://www.ccad.ac.uk',
  resource.handle = 'cleveland-college-of-art',
  resource.index_data = 'C414 C420 O100 A630 A530 D225',
  resource.quarter = 20174,
  resource.summary = 'university/zlwog1tffxyapadzxzb6',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611520/university/zlwog1tffxyapadzxzb6.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Cleveland College of Art and Design';

UPDATE resource
SET resource.homepage = 'https://www.cliffcollege.ac.uk',
  resource.handle = 'cliff-college',
  resource.index_data = 'C410 C420',
  resource.quarter = 20174,
  resource.summary = 'university/lg2awxrkbmazxwlgxgkv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611521/university/lg2awxrkbmazxwlgxgkv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Cliff College';

UPDATE resource
SET resource.homepage = 'https://www.colchester.ac.uk',
  resource.handle = 'colchester-institute',
  resource.index_data = 'C422 I523',
  resource.quarter = 20174,
  resource.summary = 'university/vqjlisoufririwxsb0xj',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611523/university/vqjlisoufririwxsb0xj.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Colchester Institute';

UPDATE resource
SET resource.homepage = 'https://www.gllm.ac.uk',
  resource.handle = 'coleg-llandrillo',
  resource.index_data = 'C420 L536',
  resource.quarter = 20174,
  resource.summary = 'university/dqtogcgzedsunvbrgbu8',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611524/university/dqtogcgzedsunvbrgbu8.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Coleg Llandrillo';

UPDATE resource
SET resource.homepage = 'https://www.colegsirgar.ac.uk',
  resource.handle = 'coleg-sir-gar',
  resource.index_data = 'C420 S600 G600',
  resource.quarter = 20174,
  resource.summary = 'university/kvyqpqb6osc5cgtcketv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611526/university/kvyqpqb6osc5cgtcketv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Coleg Sir Gar';

UPDATE resource
SET resource.homepage = 'https://www.cwa.ac.uk',
  resource.handle = 'college-of-west-anglia',
  resource.index_data = 'C420 O100 W230 A524',
  resource.quarter = 20174,
  resource.summary = 'university/judmftcb9ctjglulos9e',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611527/university/judmftcb9ctjglulos9e.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'College of West Anglia';

UPDATE resource
SET resource.homepage = 'https://www.cdd.ac.uk',
  resource.handle = 'conservatoire-for-dance',
  resource.index_data = 'C526 F600 D520 A530 D650',
  resource.quarter = 20174,
  resource.summary = 'university/qmnrgk7jkximustdajqj',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611529/university/qmnrgk7jkximustdajqj.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Conservatoire for Dance and Drama';

UPDATE resource
SET resource.homepage = 'https://www.cornwall.ac.uk',
  resource.handle = 'cornwall-college',
  resource.index_data = 'C654 C420',
  resource.quarter = 20174,
  resource.summary = 'university/qfglf0338hnuh2n23dvj',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611531/university/qfglf0338hnuh2n23dvj.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Cornwall College';

UPDATE resource
SET resource.homepage = 'https://www.courtauld.ac.uk',
  resource.handle = 'courtauld-institute-of',
  resource.index_data = 'C634 I523 O100 A630',
  resource.quarter = 20174,
  resource.summary = 'university/tqz7xwzasipsm6nqxrxw',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611532/university/tqz7xwzasipsm6nqxrxw.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Courtauld Institute of Art';

UPDATE resource
SET resource.homepage = 'https://www.coventry.ac.uk',
  resource.handle = 'coventry-university',
  resource.index_data = 'C153 U516',
  resource.quarter = 20174,
  resource.summary = 'university/h2flfmmmikzkd26zj235',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611534/university/h2flfmmmikzkd26zj235.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Coventry University';

UPDATE resource
SET resource.homepage = 'https://www.dmu.ac.uk',
  resource.handle = 'de-montfort-university',
  resource.index_data = 'D000 M531 U516',
  resource.quarter = 20174,
  resource.summary = 'university/gvujqart1eisqrxcjyqu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611538/university/gvujqart1eisqrxcjyqu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'De Montfort University';

UPDATE resource
SET resource.homepage = 'https://www.dearne-coll.ac.uk',
  resource.handle = 'dearne-valley-college',
  resource.index_data = 'D650 V400 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ka2ke10l0u66fdiwaatk',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611539/university/ka2ke10l0u66fdiwaatk.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Dearne Valley College';

UPDATE resource
SET resource.homepage = 'https://www.don.ac.uk',
  resource.handle = 'doncaster-college',
  resource.index_data = 'D522 C420',
  resource.quarter = 20174,
  resource.summary = 'university/foldiu23p3ux4toa0nnw',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611543/university/foldiu23p3ux4toa0nnw.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Doncaster College';

UPDATE resource
SET resource.homepage = 'https://www.duchy.ac.uk',
  resource.handle = 'duchy-college',
  resource.index_data = 'D200 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ds15um0cet5gfz5dlmgt',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611544/university/ds15um0cet5gfz5dlmgt.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Duchy College';

UPDATE resource
SET resource.homepage = 'https://www.dudleycol.ac.uk',
  resource.handle = 'dudley-college-of',
  resource.index_data = 'D340 C420 O100 T254',
  resource.quarter = 20174,
  resource.summary = 'university/fvneu2rs5farhdypubhv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611546/university/fvneu2rs5farhdypubhv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Dudley College of Technology';

UPDATE resource
SET resource.homepage = 'https://www.durham.ac.uk',
  resource.handle = 'durham-university',
  resource.index_data = 'D650 U516',
  resource.quarter = 20174,
  resource.summary = 'university/hxkcfpw2yt7n4pwg2xxe',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611548/university/hxkcfpw2yt7n4pwg2xxe.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Durham University';

UPDATE resource
SET resource.homepage = 'https://www.wlc.ac.uk',
  resource.handle = 'hammersmith-and-west',
  resource.index_data = 'E452 H562 A530 W230 L535 C420',
  resource.quarter = 20174,
  resource.summary = 'university/hfeu42wkznktoxbvswq1',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611549/university/hfeu42wkznktoxbvswq1.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Ealing, Hammersmith and West London College';

UPDATE resource
SET resource.homepage = 'https://www.eastridingcollege.ac.uk',
  resource.handle = 'east-riding-college',
  resource.index_data = 'E230 R352 C420',
  resource.quarter = 20174,
  resource.summary = 'university/twl7wk3c0qvluan2y6oo',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611553/university/twl7wk3c0qvluan2y6oo.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'East Riding College';

UPDATE resource
SET resource.homepage = 'https://www.esc.ac.uk',
  resource.handle = 'east-surrey-college',
  resource.index_data = 'E230 S600 C420',
  resource.quarter = 20174,
  resource.summary = 'university/rxe9disljb7w2rnwbmuy',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611556/university/rxe9disljb7w2rnwbmuy.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'East Surrey College';

UPDATE resource
SET resource.homepage = 'https://www.edgehill.ac.uk',
  resource.handle = 'edge-hill-university',
  resource.index_data = 'E320 H400 U516',
  resource.quarter = 20174,
  resource.summary = 'university/nlpxbe1okmoiocipsbzy',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611560/university/nlpxbe1okmoiocipsbzy.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Edge Hill University';

UPDATE resource
SET resource.homepage = 'https://www.edgehotelschool.ac.uk',
  resource.handle = 'edge-hotel-school',
  resource.index_data = 'E320 H340 S400',
  resource.quarter = 20174,
  resource.summary = 'university/rjtvgz2zxcxzmaovrodf',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611562/university/rjtvgz2zxcxzmaovrodf.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Edge Hotel School';

UPDATE resource
SET resource.homepage = 'https://www.napier.ac.uk',
  resource.handle = 'edinburgh-napier',
  resource.index_data = 'E351 N160 U516',
  resource.quarter = 20174,
  resource.summary = 'university/vmyzlb4kte59usu91jor',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611566/university/vmyzlb4kte59usu91jor.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Edinburgh Napier University';

UPDATE resource
SET resource.homepage = 'https://www.eso.ac.uk',
  resource.handle = 'european-school-of-2',
  resource.index_data = 'E615 S400 O100 O231',
  resource.quarter = 20174,
  resource.summary = 'university/mbytw8ww7cft4gphdy6d',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611570/university/mbytw8ww7cft4gphdy6d.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'European School of Osteopathy';

UPDATE resource
SET resource.homepage = 'https://www.exe-coll.ac.uk',
  resource.handle = 'exeter-college',
  resource.index_data = 'E236 C420',
  resource.quarter = 20174,
  resource.summary = 'university/llijiceavu8a1jwoochs',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611571/university/llijiceavu8a1jwoochs.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Exeter College';

UPDATE resource
SET resource.homepage = 'https://www.fareham.ac.uk',
  resource.handle = 'fareham-college',
  resource.index_data = 'F650 C420',
  resource.quarter = 20174,
  resource.summary = 'university/alwjl6r7pezt0lmt5fll',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611574/university/alwjl6r7pezt0lmt5fll.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Fareham College';

UPDATE resource
SET resource.homepage = 'https://www.furnesscollege.co.uk',
  resource.handle = 'furness-college',
  resource.index_data = 'F652 C420',
  resource.quarter = 20174,
  resource.summary = 'university/uz2b4f92oi4m43aorwpe',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611575/university/uz2b4f92oi4m43aorwpe.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Furness College';

UPDATE resource
SET resource.homepage = 'https://www.gateshead.ac.uk',
  resource.handle = 'gateshead-college',
  resource.index_data = 'G323 C420',
  resource.quarter = 20174,
  resource.summary = 'university/tg3s4kwjn507iapakakz',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611577/university/tg3s4kwjn507iapakakz.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Gateshead College';

UPDATE resource
SET resource.homepage = 'https://www.gcu.ac.uk',
  resource.handle = 'glasgow-caledonian',
  resource.index_data = 'G420 C435 U516',
  resource.quarter = 20174,
  resource.summary = 'university/edynlrdsfjvgd3hkslvx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611579/university/edynlrdsfjvgd3hkslvx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Glasgow Caledonian University';

UPDATE resource
SET resource.homepage = 'https://www.northglasgowcollege.ac.uk',
  resource.handle = 'glasgow-kelvin-college',
  resource.index_data = 'G420 K415 C420',
  resource.quarter = 20174,
  resource.summary = 'university/nonfmcqfblcrfzg37qzk',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611581/university/nonfmcqfblcrfzg37qzk.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Glasgow Kelvin College';

UPDATE resource
SET resource.homepage = 'https://www.gsa.ac.uk',
  resource.handle = 'glasgow-school-of-art',
  resource.index_data = 'G420 S400 O100 A630',
  resource.quarter = 20174,
  resource.summary = 'university/gr287jjilcvjd5zok4ng',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611583/university/gr287jjilcvjd5zok4ng.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Glasgow School of Art';

UPDATE resource
SET resource.homepage = 'https://www.gloscol.ac.uk',
  resource.handle = 'gloucestershire-college',
  resource.index_data = 'G422 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ocvrwkuyuef0dlep5uza',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611584/university/ocvrwkuyuef0dlep5uza.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Gloucestershire College';

UPDATE resource
SET resource.homepage = 'https://www.glyndwr.ac.uk',
  resource.handle = 'glyndwr-university',
  resource.index_data = 'G453 U516',
  resource.quarter = 20174,
  resource.summary = 'university/lpvka5srhjujdzqvudb8',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611586/university/lpvka5srhjujdzqvudb8.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Glyndwr University';

UPDATE resource
SET resource.homepage = 'https://www.gold.ac.uk',
  resource.handle = 'university-of-london',
  resource.index_data = 'G432 U516 O100 L535',
  resource.quarter = 20174,
  resource.summary = 'university/noxlmfjdszf2yyyx8noc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611588/university/noxlmfjdszf2yyyx8noc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Goldsmiths, University of London';

UPDATE resource
SET resource.homepage = 'https://www.gowercollegeswansea.ac.uk',
  resource.handle = 'gower-college-swansea',
  resource.index_data = 'G600 C420 S520',
  resource.quarter = 20174,
  resource.summary = 'university/rzqxu7ov6s2o48nplmse',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611590/university/rzqxu7ov6s2o48nplmse.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Gower College Swansea';

UPDATE resource
SET resource.homepage = 'https://www.nptcgroup.ac.uk',
  resource.handle = 'grwp-colegau',
  resource.index_data = 'G610 C420',
  resource.quarter = 20174,
  resource.summary = 'university/wjj3ln11h8fjv2mut5iz',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611591/university/wjj3ln11h8fjv2mut5iz.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Grŵp Colegau';

UPDATE resource
SET resource.homepage = 'https://www.gsm.org.uk',
  resource.handle = 'greenwich-school-of',
  resource.index_data = 'G652 S400 O100 M525 G500 L535',
  resource.quarter = 20174,
  resource.summary = 'university/o15cq58cuqhbpb6net7e',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611592/university/o15cq58cuqhbpb6net7e.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Greenwich School of Management (GSM London)';

UPDATE resource
SET resource.homepage = 'https://www.gsmd.ac.uk',
  resource.handle = 'guildhall-school-of-music',
  resource.index_data = 'G434 S400 O100 M220 A530 D650',
  resource.quarter = 20174,
  resource.summary = 'university/breyhiuqldtsiyusoe07',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611595/university/breyhiuqldtsiyusoe07.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Guildhall School of Music and Drama';

UPDATE resource
SET resource.homepage = 'https://www.hackney.ac.uk',
  resource.handle = 'hackney-community-college',
  resource.index_data = 'H250 C553 C420',
  resource.quarter = 20174,
  resource.summary = 'university/eqmdynp3u2hknikf0aux',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611596/university/eqmdynp3u2hknikf0aux.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Hackney Community College';

UPDATE resource
SET resource.homepage = 'https://www.hadlow.ac.uk',
  resource.handle = 'hadlow-college',
  resource.index_data = 'H340 C420',
  resource.quarter = 20174,
  resource.summary = 'university/wknwgvgqhqcqqykyahp1',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611598/university/wknwgvgqhqcqqykyahp1.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Hadlow College';

UPDATE resource
SET resource.homepage = 'https://www.harper-adams.ac.uk',
  resource.handle = 'harper-adams-university',
  resource.index_data = 'H616 A352 U516',
  resource.quarter = 20174,
  resource.summary = 'university/cr8dcv6w022mnzqcmdly',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611599/university/cr8dcv6w022mnzqcmdly.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Harper Adams University';

UPDATE resource
SET resource.homepage = 'https://www.harrogate.ac.uk',
  resource.handle = 'harrogate-college',
  resource.index_data = 'H623 C420',
  resource.quarter = 20174,
  resource.summary = 'university/wauaqoljiochlytodhcv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611601/university/wauaqoljiochlytodhcv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Harrogate College';

UPDATE resource
SET resource.homepage = 'https://www.harrow.ac.uk',
  resource.handle = 'harrow-college',
  resource.index_data = 'H600 C420',
  resource.quarter = 20174,
  resource.summary = 'university/nbvtxwwzn4tpegktz4fe',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611602/university/nbvtxwwzn4tpegktz4fe.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Harrow College';

UPDATE resource
SET resource.homepage = 'https://www.hartpury.ac.uk',
  resource.handle = 'hartpury-college',
  resource.index_data = 'H631 C420',
  resource.quarter = 20174,
  resource.summary = 'university/xp8l80trnanosyhd2mgw',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611603/university/xp8l80trnanosyhd2mgw.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Hartpury College';

UPDATE resource
SET resource.homepage = 'https://www.howcollege.ac.uk',
  resource.handle = 'heart-of-worcestershire',
  resource.index_data = 'H630 O100 W622 C420',
  resource.quarter = 20174,
  resource.summary = 'university/pin7lkxtd9wcbrrflad0',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611607/university/pin7lkxtd9wcbrrflad0.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Heart of Worcestershire College';

UPDATE resource
SET resource.homepage = 'https://www.henley-cov.ac.uk',
  resource.handle = 'henley-college-coventry',
  resource.index_data = 'H540 C420 C153',
  resource.quarter = 20174,
  resource.summary = 'university/o2bkird6cdrot3k0g3je',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611608/university/o2bkird6cdrot3k0g3je.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Henley College Coventry';

UPDATE resource
SET resource.homepage = 'https://www.hca.ac.uk',
  resource.handle = 'hereford-college-of-arts',
  resource.index_data = 'H616 C420 O100 A632',
  resource.quarter = 20174,
  resource.summary = 'university/ckondo97fcsz1lsxmd6m',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611609/university/ckondo97fcsz1lsxmd6m.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Hereford College of Arts';

UPDATE resource
SET resource.homepage = 'https://www.hw.ac.uk',
  resource.handle = 'university',
  resource.index_data = 'H630 W300 U516',
  resource.quarter = 20174,
  resource.summary = 'university/emnr52tuat77rlibqels',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611611/university/emnr52tuat77rlibqels.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Heriot-Watt University';

UPDATE resource
SET resource.homepage = 'https://www.hrc.ac.uk',
  resource.handle = 'hertford-regional-college',
  resource.index_data = 'H631 R254 C420',
  resource.quarter = 20174,
  resource.summary = 'university/bt5x0lcsvu9e4mxp2knw',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611612/university/bt5x0lcsvu9e4mxp2knw.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Hertford Regional College';

UPDATE resource
SET resource.homepage = 'https://www.heythrop.ac.uk',
  resource.handle = 'heythrop-college',
  resource.index_data = 'H361 C420',
  resource.quarter = 20174,
  resource.summary = 'university/uzuyumfygk2tb7dnjmne',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611613/university/uzuyumfygk2tb7dnjmne.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Heythrop College';

UPDATE resource
SET resource.homepage = 'https://www.highbury.ac.uk',
  resource.handle = 'highbury-college',
  resource.index_data = 'H216 C420',
  resource.quarter = 20174,
  resource.summary = 'university/uqgccqts2pk8yywehn8d',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611614/university/uqgccqts2pk8yywehn8d.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Highbury College';

UPDATE resource
SET resource.homepage = 'https://www.holycross.ac.uk',
  resource.handle = 'holy-cross-sixth-form',
  resource.index_data = 'H400 C620 S230 F650 C420 A530 U516 C536',
  resource.quarter = 20174,
  resource.summary = 'university/xgvl5ppfw6n7e6g2gpey',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611616/university/xgvl5ppfw6n7e6g2gpey.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Holy Cross Sixth Form College and University Centre';

UPDATE resource
SET resource.homepage = 'https://www.hopwood.ac.uk',
  resource.handle = 'hopwood-hall-college',
  resource.index_data = 'H130 H400 C420',
  resource.quarter = 20174,
  resource.summary = 'university/rhkdc3ye3rbol8rqq6pp',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611617/university/rhkdc3ye3rbol8rqq6pp.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Hopwood Hall College';

UPDATE resource
SET resource.homepage = 'https://www.hull-college.ac.uk',
  resource.handle = 'hull-college',
  resource.index_data = 'H400 C420',
  resource.quarter = 20174,
  resource.summary = 'university/nlkqqikg3ldmufudj9ew',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611619/university/nlkqqikg3ldmufudj9ew.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Hull College';

UPDATE resource
SET resource.homepage = 'https://www.hyms.ac.uk',
  resource.handle = 'hull-york-medical-school',
  resource.index_data = 'H400 Y620 M324 S400',
  resource.quarter = 20174,
  resource.summary = 'university/s55f2byycyuxijwd0t3y',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611621/university/s55f2byycyuxijwd0t3y.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Hull York Medical School';

UPDATE resource
SET resource.homepage = 'https://www.ifslearning.ac.uk',
  resource.handle = 'ifs-university-college',
  resource.index_data = 'I120 U516 C420',
  resource.quarter = 20174,
  resource.summary = 'university/wc1uuvbq4yfd2mxzs3yy',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611622/university/wc1uuvbq4yfd2mxzs3yy.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Ifs University College';

UPDATE resource
SET resource.homepage = 'https://www.imperial.ac.uk',
  resource.handle = 'imperial-college-london',
  resource.index_data = 'I516 C420 L535',
  resource.quarter = 20174,
  resource.summary = 'university/ecmluxywhaeomyeng1k5',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611624/university/ecmluxywhaeomyeng1k5.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Imperial College London';

UPDATE resource
SET resource.homepage = 'https://www.islamic-college.ac.uk',
  resource.handle = 'islamic-college-for',
  resource.index_data = 'I245 C420 F600 A315 S332',
  resource.quarter = 20174,
  resource.summary = 'university/lsw19neggbmqfjrdzktb',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611625/university/lsw19neggbmqfjrdzktb.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Islamic College for Advanced Studies';

UPDATE resource
SET resource.homepage = 'https://www.istitutomarangoni.com',
  resource.handle = 'istituto-marangoni',
  resource.index_data = 'I233 M652',
  resource.quarter = 20174,
  resource.summary = 'university/rwqtcqj06ebyra6336gk',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611627/university/rwqtcqj06ebyra6336gk.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Istituto Marangoni';

UPDATE resource
SET resource.homepage = 'https://www.johnruskin.ac.uk',
  resource.handle = 'john-ruskin-college',
  resource.index_data = 'J500 R250 C420',
  resource.quarter = 20174,
  resource.summary = 'university/qvs9z73lxbkgyrrjjbcv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611628/university/qvs9z73lxbkgyrrjjbcv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'John Ruskin College';

UPDATE resource
SET resource.homepage = 'https://www.holborncollege.ac.uk',
  resource.handle = 'kaplan-holborn-college',
  resource.index_data = 'K145 H416 C420',
  resource.quarter = 20174,
  resource.summary = 'university/yvzwrib1fwkozaybvvn9',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611629/university/yvzwrib1fwkozaybvvn9.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Kaplan Holborn College';

UPDATE resource
SET resource.homepage = 'https://www.keele.ac.uk',
  resource.handle = 'keele-university',
  resource.index_data = 'K400 U516',
  resource.quarter = 20174,
  resource.summary = 'university/cpztk2xfyo5yqlyrsf2c',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611630/university/cpztk2xfyo5yqlyrsf2c.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Keele University';

UPDATE resource
SET resource.homepage = 'https://www.kcc.ac.uk',
  resource.handle = 'kensington-and-chelsea',
  resource.index_data = 'K525 A530 C420 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ynrjgtfemcwwtvdt2u5e',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611632/university/ynrjgtfemcwwtvdt2u5e.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Kensington and Chelsea College';

UPDATE resource
SET resource.homepage = 'https://www.kensingtoncoll.ac.uk',
  resource.handle = 'kensington-college-of',
  resource.index_data = 'K525 C420 O100 B252',
  resource.quarter = 20174,
  resource.summary = 'university/ensf7u0rca28evyoo6ry',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611633/university/ensf7u0rca28evyoo6ry.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Kensington College of Business';

UPDATE resource
SET resource.homepage = 'https://www.kcl.ac.uk',
  resource.handle = 'kings-college-london',
  resource.index_data = 'K520 C420 L535',
  resource.quarter = 20174,
  resource.summary = 'university/fz4ua2kkwvxrmw2gellk',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611634/university/fz4ua2kkwvxrmw2gellk.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Kings College London';

UPDATE resource
SET resource.homepage = 'https://www.kingston-college.ac.uk',
  resource.handle = 'kingston-college',
  resource.index_data = 'K523 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ched1jhntukz86deaiht',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611636/university/ched1jhntukz86deaiht.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Kingston College';

UPDATE resource
SET resource.homepage = 'https://www.kmc.ac.uk',
  resource.handle = 'kingston-maurward-college',
  resource.index_data = 'K523 M663 C420',
  resource.quarter = 20174,
  resource.summary = 'university/osb9af5xaoebzo6aogey',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611637/university/osb9af5xaoebzo6aogey.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Kingston Maurward College';

UPDATE resource
SET resource.homepage = 'https://www.kingston.ac.uk',
  resource.handle = 'kingston-university',
  resource.index_data = 'K523 U516',
  resource.quarter = 20174,
  resource.summary = 'university/l2dk4comwk4uvyovabgp',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611639/university/l2dk4comwk4uvyovabgp.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Kingston University';

UPDATE resource
SET resource.homepage = 'https://www.kirkleescollege.ac.uk',
  resource.handle = 'kirklees-college',
  resource.index_data = 'K624 C420',
  resource.quarter = 20174,
  resource.summary = 'university/iob8gvki4tv321sp062l',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611641/university/iob8gvki4tv321sp062l.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Kirklees College';

UPDATE resource
SET resource.homepage = 'https://www.klc.co.uk',
  resource.handle = 'klc-school-of-design',
  resource.index_data = 'K420 S400 O100 D225',
  resource.quarter = 20174,
  resource.summary = 'university/bv76itgl1icjtdgbengb',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611642/university/bv76itgl1icjtdgbengb.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'KLC School of Design';

UPDATE resource
SET resource.homepage = 'https://www.knowsleycollege.ac.uk',
  resource.handle = 'knowsley-community',
  resource.index_data = 'K524 C553 C420',
  resource.quarter = 20174,
  resource.summary = 'university/xihoszyjjsmx7vbzjmle',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611643/university/xihoszyjjsmx7vbzjmle.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Knowsley Community College';

UPDATE resource
SET resource.homepage = 'https://www.lancaster.ac.uk',
  resource.handle = 'lancaster-university',
  resource.index_data = 'L522 U516',
  resource.quarter = 20174,
  resource.summary = 'university/tcxprdzanaqzccld6krp',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611646/university/tcxprdzanaqzccld6krp.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Lancaster University';

UPDATE resource
SET resource.homepage = 'https://www.lca.anglia.ac.uk',
  resource.handle = 'lca-business-school',
  resource.index_data = 'L200 B252 S400',
  resource.quarter = 20174,
  resource.summary = 'university/xbonmf5957jz0uslzphx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611647/university/xbonmf5957jz0uslzphx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'LCA Business School';

UPDATE resource
SET resource.homepage = 'https://www.leedsbeckett.ac.uk',
  resource.handle = 'leeds-beckett-university',
  resource.index_data = 'L320 B230 U516',
  resource.quarter = 20174,
  resource.summary = 'university/szi12tp24oqqbfkc7kzx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611648/university/szi12tp24oqqbfkc7kzx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Leeds Beckett University';

UPDATE resource
SET resource.homepage = 'https://www.leedscitycollege.ac.uk',
  resource.handle = 'leeds-city-college',
  resource.index_data = 'L320 C300 C420',
  resource.quarter = 20174,
  resource.summary = 'university/pxubgnzzrcbptwr8ldsu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611650/university/pxubgnzzrcbptwr8ldsu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Leeds City College';

UPDATE resource
SET resource.homepage = 'https://www.leeds-art.ac.uk',
  resource.handle = 'leeds-college-of-art',
  resource.index_data = 'L320 C420 O100 A630',
  resource.quarter = 20174,
  resource.summary = 'university/ndq7o6somempestzvv3c',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611652/university/ndq7o6somempestzvv3c.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Leeds College of Art';

UPDATE resource
SET resource.homepage = 'https://www.lcb.ac.uk',
  resource.handle = 'leeds-college-of-building',
  resource.index_data = 'L320 C420 O100 B435',
  resource.quarter = 20174,
  resource.summary = 'university/eazcobzgeooqgmgjpqmp',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611653/university/eazcobzgeooqgmgjpqmp.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Leeds College of Building';

UPDATE resource
SET resource.homepage = 'https://www.lcm.ac.uk',
  resource.handle = 'leeds-college-of-music',
  resource.index_data = 'L320 C420 O100 M220',
  resource.quarter = 20174,
  resource.summary = 'university/kvtprkqgnpayefya43ma',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611654/university/kvtprkqgnpayefya43ma.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Leeds College of Music';

UPDATE resource
SET resource.homepage = 'https://www.leedstrinity.ac.uk',
  resource.handle = 'leeds-trinity-university',
  resource.index_data = 'L320 T653 U516',
  resource.quarter = 20174,
  resource.summary = 'university/xu4qamno7oiv0zo27mn7',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611655/university/xu4qamno7oiv0zo27mn7.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Leeds Trinity University';

UPDATE resource
SET resource.homepage = 'https://www.leicestercollege.ac.uk',
  resource.handle = 'leicester-college',
  resource.index_data = 'L223 C420',
  resource.quarter = 20174,
  resource.summary = 'university/q03efhovdwdh2s2a1bca',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611656/university/q03efhovdwdh2s2a1bca.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Leicester College';

UPDATE resource
SET resource.homepage = 'https://www.lesoco.ac.uk',
  resource.handle = 'lewisham-southwark',
  resource.index_data = 'L250 S362 C420',
  resource.quarter = 20174,
  resource.summary = 'university/i1r9sbkqon4soebpnotf',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611658/university/i1r9sbkqon4soebpnotf.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Lewisham Southwark College';

UPDATE resource
SET resource.homepage = 'https://www.lincolncollege.ac.uk',
  resource.handle = 'lincoln-college',
  resource.index_data = 'L524 C420',
  resource.quarter = 20174,
  resource.summary = 'university/kqk0tzksn8l7nq4gaasl',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611659/university/kqk0tzksn8l7nq4gaasl.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Lincoln College';

UPDATE resource
SET resource.homepage = 'https://www.hope.ac.uk',
  resource.handle = 'liverpool-hope-university',
  resource.index_data = 'L161 H100 U516',
  resource.quarter = 20174,
  resource.summary = 'university/iy7lzznxgzyniy24umo1',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611660/university/iy7lzznxgzyniy24umo1.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Liverpool Hope University';

UPDATE resource
SET resource.homepage = 'https://www.ljmu.ac.uk',
  resource.handle = 'liverpool-john-moores',
  resource.index_data = 'L161 J500 M620 U516',
  resource.quarter = 20174,
  resource.summary = 'university/jaolqlazqovxgwxb9nv7',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611661/university/jaolqlazqovxgwxb9nv7.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Liverpool John Moores University';

UPDATE resource
SET resource.homepage = 'https://www.london.edu',
  resource.handle = 'london-business-school',
  resource.index_data = 'L535 B252 S400',
  resource.quarter = 20174,
  resource.summary = 'university/thbtr7iswirdynqkavtd',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611663/university/thbtr7iswirdynqkavtd.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London Business School';

UPDATE resource
SET resource.homepage = 'https://www.londonmet.ac.uk',
  resource.handle = 'london-metropolitan',
  resource.index_data = 'L535 M361 U516',
  resource.quarter = 20174,
  resource.summary = 'university/s9lw0bgegk3lwvgvamfy',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611667/university/s9lw0bgegk3lwvgvamfy.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London Metropolitan University';

UPDATE resource
SET resource.homepage = 'https://www.lsbm.ac.uk',
  resource.handle = 'london-school-of-business',
  resource.index_data = 'L535 S400 O100 B252 A530 M525',
  resource.quarter = 20174,
  resource.summary = 'university/ky1bcqfuqluocyweiyv7',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611668/university/ky1bcqfuqluocyweiyv7.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London School of Business and Management';

UPDATE resource
SET resource.homepage = 'https://www.lsclondon.co.uk',
  resource.handle = 'london-school-of-commerce',
  resource.index_data = 'L535 S400 O100 C562',
  resource.quarter = 20174,
  resource.summary = 'university/bkkmx21w7y6tqhnuyg7h',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611669/university/bkkmx21w7y6tqhnuyg7h.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London School of Commerce';

UPDATE resource
SET resource.homepage = 'https://www.lse.ac.uk',
  resource.handle = 'london-school-of',
  resource.index_data = 'L535 S400 O100 E255 A530 P432 S520',
  resource.quarter = 20174,
  resource.summary = 'university/vyrufsxp7yx1ayvewatl',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611671/university/vyrufsxp7yx1ayvewatl.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London School of Economics and Political Science';

UPDATE resource
SET resource.homepage = 'https://www.lshtm.ac.uk',
  resource.handle = 'london-school-of-hygiene',
  resource.index_data = 'L535 S400 O100 H250 A530 T612 M325',
  resource.quarter = 20174,
  resource.summary = 'university/czelnmsix893srl3vkt6',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611672/university/czelnmsix893srl3vkt6.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London School of Hygiene and Tropical Medicine';

UPDATE resource
SET resource.homepage = 'https://www.londonschoolofmarketing.com',
  resource.handle = 'london-school-of-2',
  resource.index_data = 'L535 S400 O100 M623',
  resource.quarter = 20174,
  resource.summary = 'university/fxne0ig1tff6n9zhc6xl',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611673/university/fxne0ig1tff6n9zhc6xl.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London School of Marketing';

UPDATE resource
SET resource.homepage = 'https://www.lsst.ac',
  resource.handle = 'london-school-of-science',
  resource.index_data = 'L535 S400 O100 S520 A530 T254',
  resource.quarter = 20174,
  resource.summary = 'university/vbtnaiwn8jqhl8btcgsn',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611675/university/vbtnaiwn8jqhl8btcgsn.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London School of Science and Technology';

UPDATE resource
SET resource.homepage = 'https://www.lsbu.ac.uk',
  resource.handle = 'london-south-bank',
  resource.index_data = 'L535 S300 B520 U516',
  resource.quarter = 20174,
  resource.summary = 'university/ycqdx1svmnzlym6gmkzm',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611676/university/ycqdx1svmnzlym6gmkzm.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London South Bank University';

UPDATE resource
SET resource.homepage = 'https://www.loucoll.ac.uk',
  resource.handle = 'loughborough-college',
  resource.index_data = 'L216 C420',
  resource.quarter = 20174,
  resource.summary = 'university/mfoufhxtki6wgk3m4lo5',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611678/university/mfoufhxtki6wgk3m4lo5.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Loughborough College';

UPDATE resource
SET resource.homepage = 'https://www.lboro.ac.uk',
  resource.handle = 'loughborough-university',
  resource.index_data = 'L216 U516',
  resource.quarter = 20174,
  resource.summary = 'university/uon2w2ifkxqlybxwlcd1',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611679/university/uon2w2ifkxqlybxwlcd1.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Loughborough University';

UPDATE resource
SET resource.homepage = 'https://www.macclesfield.ac.uk',
  resource.handle = 'macclesfield-college',
  resource.index_data = 'M242 C420',
  resource.quarter = 20174,
  resource.summary = 'university/twrrl8nvhfqgowuoryjl',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611680/university/twrrl8nvhfqgowuoryjl.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Macclesfield College';

UPDATE resource
SET resource.homepage = 'https://www.msp.ac.uk',
  resource.handle = 'medway-school-of-pharmacy',
  resource.index_data = 'M300 S400 O100 P652',
  resource.quarter = 20174,
  resource.summary = 'university/iwmw5oqysgmnt63h86s3',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611682/university/iwmw5oqysgmnt63h86s3.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Medway School of Pharmacy';

UPDATE resource
SET resource.homepage = 'https://www.metfilmschool.ac.uk',
  resource.handle = 'met-film-school',
  resource.index_data = 'M300 F450 S400',
  resource.quarter = 20174,
  resource.summary = 'university/ne1plzyngo1clqwfakwc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611683/university/ne1plzyngo1clqwfakwc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Met Film School';

UPDATE resource
SET resource.homepage = 'https://www.metanoia.ac.uk',
  resource.handle = 'metanoia-institute',
  resource.index_data = 'M350 I523',
  resource.quarter = 20174,
  resource.summary = 'university/vfgdtiiw4fcrcmue6bwu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611684/university/vfgdtiiw4fcrcmue6bwu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Metanoia Institute';

UPDATE resource
SET resource.homepage = 'https://www.mdx.ac.uk',
  resource.handle = 'middlesex-university',
  resource.index_data = 'M342 U516',
  resource.quarter = 20174,
  resource.summary = 'university/qe0jbpdy0zkmuyrhnc4i',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611687/university/qe0jbpdy0zkmuyrhnc4i.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Middlesex University';

UPDATE resource
SET resource.homepage = 'https://www.midkent.ac.uk',
  resource.handle = 'midkent-college',
  resource.index_data = 'M325 C420',
  resource.quarter = 20174,
  resource.summary = 'university/x1fbpxtzofv6ksaqgm5y',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611688/university/x1fbpxtzofv6ksaqgm5y.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'MidKent College';

UPDATE resource
SET resource.homepage = 'https://www.mkcollege.ac.uk',
  resource.handle = 'milton-keynes-college',
  resource.index_data = 'M435 K520 C420',
  resource.quarter = 20174,
  resource.summary = 'university/frbximqkxmsn6ebz7s5s',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611689/university/frbximqkxmsn6ebz7s5s.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Milton Keynes College';

UPDATE resource
SET resource.homepage = 'https://www.mrcollege.ac.uk',
  resource.handle = 'mont-rose-college',
  resource.index_data = 'M530 R200 C420',
  resource.quarter = 20174,
  resource.summary = 'university/q9m5uw6kd4newbjqbyw7',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611690/university/q9m5uw6kd4newbjqbyw7.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Mont Rose College';

UPDATE resource
SET resource.homepage = 'https://www.moulton.ac.uk',
  resource.handle = 'moulton-college',
  resource.index_data = 'M435 C420',
  resource.quarter = 20174,
  resource.summary = 'university/msaaveyjyvvgp1vgzpnm',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611691/university/msaaveyjyvvgp1vgzpnm.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Moulton College';

UPDATE resource
SET resource.homepage = 'https://www.mountview.org.uk',
  resource.handle = 'mountview-academy-of',
  resource.index_data = 'M531 A235 O100 T360 A632',
  resource.quarter = 20174,
  resource.summary = 'university/b5bj7kzbgarfposzbubx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611693/university/b5bj7kzbgarfposzbubx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Mountview Academy of Theatre Arts';

UPDATE resource
SET resource.homepage = 'https://www.myerscough.ac.uk',
  resource.handle = 'myerscough-college',
  resource.index_data = 'M622 C420',
  resource.quarter = 20174,
  resource.summary = 'university/mxswo8csbvwtlcljlnya',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611694/university/mxswo8csbvwtlcljlnya.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Myerscough College';

UPDATE resource
SET resource.homepage = 'https://www.nazarene.ac.uk',
  resource.handle = 'nazarene-theological',
  resource.index_data = 'N265 T422 C420',
  resource.quarter = 20174,
  resource.summary = 'university/tdy93iauxri1tlhrxuat',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611695/university/tdy93iauxri1tlhrxuat.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Nazarene Theological College';

UPDATE resource
SET resource.homepage = 'https://www.nescot.ac.uk',
  resource.handle = 'nescot-college',
  resource.index_data = 'N230 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ua3vmeeldxqfey6kstcv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611697/university/ua3vmeeldxqfey6kstcv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Nescot College';

UPDATE resource
SET resource.homepage = 'https://www.newcollegedurham.ac.uk',
  resource.handle = 'new-college-durham',
  resource.index_data = 'N000 C420 D650',
  resource.quarter = 20174,
  resource.summary = 'university/zc0ado9bi9xknjbxyqcu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611699/university/zc0ado9bi9xknjbxyqcu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'New College Durham';

UPDATE resource
SET resource.homepage = 'https://www.nchlondon.ac.uk',
  resource.handle = 'new-college-of-the',
  resource.index_data = 'N000 C420 O100 T000 H553',
  resource.quarter = 20174,
  resource.summary = 'university/yl7pwgahggiln58u7gtc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611701/university/yl7pwgahggiln58u7gtc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'New College of the Humanities';

UPDATE resource
SET resource.homepage = 'https://www.stamford.ac.uk',
  resource.handle = 'new-college-stamford',
  resource.index_data = 'N000 C420 S351',
  resource.quarter = 20174,
  resource.summary = 'university/vwtzadb6fz0l9bu6tmve',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611703/university/vwtzadb6fz0l9bu6tmve.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'New College Stamford';

UPDATE resource
SET resource.homepage = 'https://www.nct.ac.uk',
  resource.handle = 'new-college-telford',
  resource.index_data = 'N000 C420 T416',
  resource.quarter = 20174,
  resource.summary = 'university/coche7ue7jakuey0llm8',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611705/university/coche7ue7jakuey0llm8.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'New College Telford';

UPDATE resource
SET resource.homepage = 'https://www.newcastlecollege.co.uk',
  resource.handle = 'newcastle-college',
  resource.index_data = 'N223 C420',
  resource.quarter = 20174,
  resource.summary = 'university/p5wskcajyv2hmoeicsz3',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611706/university/p5wskcajyv2hmoeicsz3.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Newcastle College';

UPDATE resource
SET resource.homepage = 'https://www.newham.ac.uk',
  resource.handle = 'newham-college',
  resource.index_data = 'N500 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ixzvtb7vem7ccdvtohcg',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611707/university/ixzvtb7vem7ccdvtohcg.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Newham College';

UPDATE resource
SET resource.homepage = 'https://www.newman.ac.uk',
  resource.handle = 'newman-university',
  resource.index_data = 'N550 U516',
  resource.quarter = 20174,
  resource.summary = 'university/beiqddqn3fejhbkvkaw7',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611708/university/beiqddqn3fejhbkvkaw7.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Newman University';

UPDATE resource
SET resource.homepage = 'https://www.norland.co.uk',
  resource.handle = 'norland-college',
  resource.index_data = 'N645 C420',
  resource.quarter = 20174,
  resource.summary = 'university/gfl7ig4nuiu6gy4fwnln',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611710/university/gfl7ig4nuiu6gy4fwnln.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Norland College';

UPDATE resource
SET resource.homepage = 'https://www.nhc.ac.uk',
  resource.handle = 'north-hertfordshire',
  resource.index_data = 'N630 H631 C420',
  resource.quarter = 20174,
  resource.summary = 'university/urnipyblnwykmaoakpgn',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611711/university/urnipyblnwykmaoakpgn.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'North Hertfordshire College';

UPDATE resource
SET resource.homepage = 'https://www.northlindsey.ac.uk',
  resource.handle = 'north-lindsey-college',
  resource.index_data = 'N630 L532 C420',
  resource.quarter = 20174,
  resource.summary = 'university/v0th4xfcja3dd2x7khaw',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611712/university/v0th4xfcja3dd2x7khaw.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'North Lindsey College';

UPDATE resource
SET resource.homepage = 'https://www.nwhc.ac.uk',
  resource.handle = 'north-warwickshire-and',
  resource.index_data = 'N630 W626 A530 H524 C420',
  resource.quarter = 20174,
  resource.summary = 'university/wiqbbhobrjmdr1ks4sqn',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611713/university/wiqbbhobrjmdr1ks4sqn.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'North Warwickshire and Hinckley College';

UPDATE resource
SET resource.homepage = 'https://www.northkent.ac.uk',
  resource.handle = 'north-west-kent-college',
  resource.index_data = 'N630 W230 K530 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ki4u5maeba6nx2fpfz3h',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611714/university/ki4u5maeba6nx2fpfz3h.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'North West Kent College';

UPDATE resource
SET resource.homepage = 'https://www.northbrook.ac.uk',
  resource.handle = 'northbrook-college-sussex',
  resource.index_data = 'N631 C420 S220',
  resource.quarter = 20174,
  resource.summary = 'university/shmbmrlekyod3kfmyoqf',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611716/university/shmbmrlekyod3kfmyoqf.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Northbrook College Sussex';

UPDATE resource
SET resource.homepage = 'https://www.northumberland.ac.uk',
  resource.handle = 'northumberland-college',
  resource.index_data = 'N635 C420',
  resource.quarter = 20174,
  resource.summary = 'university/lqumjnuc6xg6dshuytqd',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611717/university/lqumjnuc6xg6dshuytqd.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Northumberland College';

UPDATE resource
SET resource.homepage = 'https://www.northumbria.ac.uk',
  resource.handle = 'northumbria-university',
  resource.index_data = 'N635 U516',
  resource.quarter = 20174,
  resource.summary = 'university/txhs8q4mqxi0ilc7qkto',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611719/university/txhs8q4mqxi0ilc7qkto.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Northumbria University';

UPDATE resource
SET resource.homepage = 'https://www.nortcoll.ac.uk',
  resource.handle = 'norton-radstock-college',
  resource.index_data = 'N635 R323 C420',
  resource.quarter = 20174,
  resource.summary = 'university/dxzcsapwguyc1ce5ikjg',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611720/university/dxzcsapwguyc1ce5ikjg.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Norton Radstock College';

UPDATE resource
SET resource.homepage = 'https://www.nua.ac.uk',
  resource.handle = 'norwich-university-of-the',
  resource.index_data = 'N620 U516 O100 T000 A632',
  resource.quarter = 20174,
  resource.summary = 'university/bjwgvp0mjflgouewsq17',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611721/university/bjwgvp0mjflgouewsq17.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Norwich University of the Arts';

UPDATE resource
SET resource.homepage = 'https://www.ntu.ac.uk',
  resource.handle = 'nottingham-trent',
  resource.index_data = 'N352 T653 U516',
  resource.quarter = 20174,
  resource.summary = 'university/beryfgupih6oqxiia84y',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611722/university/beryfgupih6oqxiia84y.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Nottingham Trent University';

UPDATE resource
SET resource.homepage = 'https://www.oaklands.ac.uk',
  resource.handle = 'oaklands-college',
  resource.index_data = 'O245 C420',
  resource.quarter = 20174,
  resource.summary = 'university/qal75kpcaczzd4nvypcz',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611723/university/qal75kpcaczzd4nvypcz.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Oaklands College';

UPDATE resource
SET resource.homepage = 'https://www.brookes.ac.uk',
  resource.handle = 'oxford-brookes-university',
  resource.index_data = 'O216 B622 U516',
  resource.quarter = 20174,
  resource.summary = 'university/wsppxy48x6pbe1r6nxvv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611724/university/wsppxy48x6pbe1r6nxvv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Oxford Brookes University';

UPDATE resource
SET resource.homepage = 'https://www.pearsoncollege.com',
  resource.handle = 'pearson-college',
  resource.index_data = 'P625 C420',
  resource.quarter = 20174,
  resource.summary = 'university/qmhn9grexky4kwhfklnc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611726/university/qmhn9grexky4kwhfklnc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Pearson College';

UPDATE resource
SET resource.homepage = 'https://www.pembrokeshire.ac.uk',
  resource.handle = 'pembrokeshire-college',
  resource.index_data = 'P516 C420',
  resource.quarter = 20174,
  resource.summary = 'university/yodeibpcme6kthkeyuib',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611728/university/yodeibpcme6kthkeyuib.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Pembrokeshire College';

UPDATE resource
SET resource.homepage = 'https://www.psc.ac.uk',
  resource.handle = 'peter-symonds-college',
  resource.index_data = 'P360 S553 C420 A343 A530 H260 E323',
  resource.quarter = 20174,
  resource.summary = 'university/rpj0buh58ht5tby8dbhj',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611729/university/rpj0buh58ht5tby8dbhj.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Peter Symonds College Adult and Higher Education';

UPDATE resource
SET resource.homepage = 'https://www.petroc.ac.uk',
  resource.handle = 'petroc',
  resource.index_data = 'P362',
  resource.quarter = 20174,
  resource.summary = 'university/ocmhl2tjhqlyr5kyiubo',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611731/university/ocmhl2tjhqlyr5kyiubo.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Petroc';

UPDATE resource
SET resource.homepage = 'https://www.plumpton.ac.uk',
  resource.handle = 'plumpton-college',
  resource.index_data = 'P451 C420',
  resource.quarter = 20174,
  resource.summary = 'university/y27y8ef18qmb49khabgx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611733/university/y27y8ef18qmb49khabgx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Plumpton College';

UPDATE resource
SET resource.homepage = 'https://www.plymouthart.ac.uk',
  resource.handle = 'plymouth-college-of-art',
  resource.index_data = 'P453 C420 O100 A630',
  resource.quarter = 20174,
  resource.summary = 'university/zm6dlfvsgezgahhjjaif',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611735/university/zm6dlfvsgezgahhjjaif.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Plymouth College of Art';

UPDATE resource
SET resource.homepage = 'https://www.plymouth.ac.uk',
  resource.handle = 'plymouth-university',
  resource.index_data = 'P453 U516',
  resource.quarter = 20174,
  resource.summary = 'university/tel40xlv8i5kqgarhy8j',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611737/university/tel40xlv8i5kqgarhy8j.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Plymouth University';

UPDATE resource
SET resource.homepage = 'https://www.pointblankonline.net',
  resource.handle = 'point-blank-music-school',
  resource.index_data = 'P530 B452 M220 S400',
  resource.quarter = 20174,
  resource.summary = 'university/sqwavxizxlcuasheqdmx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611739/university/sqwavxizxlcuasheqdmx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Point Blank Music School';

UPDATE resource
SET resource.homepage = 'https://www.qmu.ac.uk',
  resource.handle = 'queen-margaret-edinburgh',
  resource.index_data = 'Q500 M626 U516 E351',
  resource.quarter = 20174,
  resource.summary = 'university/vbx5cwxwulgiml48lbtt',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611742/university/vbx5cwxwulgiml48lbtt.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Queen Margaret University, Edinburgh';

UPDATE resource
SET resource.homepage = 'https://www.qmul.ac.uk',
  resource.handle = 'queen-university-of',
  resource.index_data = 'Q500 M600 U516 O100 L535',
  resource.quarter = 20174,
  resource.summary = 'university/lfdvid87whjzxu16ame2',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611745/university/lfdvid87whjzxu16ame2.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Queen Mary, University of London';

UPDATE resource
SET resource.homepage = 'https://www.qub.ac.uk',
  resource.handle = 'queens-university-belfast',
  resource.index_data = 'Q520 U516 B412',
  resource.quarter = 20174,
  resource.summary = 'university/bh9ikr0kafpnlbkdqsau',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611747/university/bh9ikr0kafpnlbkdqsau.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Queens University Belfast';

UPDATE resource
SET resource.homepage = 'https://www.ravensbourne.ac.uk',
  resource.handle = 'ravensbourne-college-of',
  resource.index_data = 'R152 C420 O100 D225 A530 C552',
  resource.quarter = 20174,
  resource.summary = 'university/fka6wa7znl0fobgnxrlc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611750/university/fka6wa7znl0fobgnxrlc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Ravensbourne College of Design and Communication';

UPDATE resource
SET resource.homepage = 'https://www.reaseheath.ac.uk',
  resource.handle = 'reaseheath-college',
  resource.index_data = 'R230 C420',
  resource.quarter = 20174,
  resource.summary = 'university/eaxx53bmzqf6ddy4fv6b',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611752/university/eaxx53bmzqf6ddy4fv6b.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Reaseheath College';

UPDATE resource
SET resource.homepage = 'https://www.regents.ac.uk',
  resource.handle = 'regents-university-london',
  resource.index_data = 'R253 U516 L535',
  resource.quarter = 20174,
  resource.summary = 'university/eo0uwnpn8ttpqmolgeq5',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611754/university/eo0uwnpn8ttpqmolgeq5.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Regents University London';

UPDATE resource
SET resource.homepage = 'https://www.rdi.co.uk',
  resource.handle = 'resource-development',
  resource.index_data = 'R262 D141 I536',
  resource.quarter = 20174,
  resource.summary = 'university/nprfvncabja7z9pklbw9',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611756/university/nprfvncabja7z9pklbw9.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Resource Development International';

UPDATE resource
SET resource.homepage = 'https://www.rutc.ac.uk',
  resource.handle = 'richmond-upon-thames',
  resource.index_data = 'R255 U150 T520 C420',
  resource.quarter = 20174,
  resource.summary = 'university/h1y8lpg06ut2opdxnwwx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611758/university/h1y8lpg06ut2opdxnwwx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Richmond Upon Thames College';

UPDATE resource
SET resource.homepage = 'https://www.richmond.ac.uk',
  resource.handle = 'the-american',
  resource.index_data = 'R255 T000 A562 I536 U516 I500 L535',
  resource.quarter = 20174,
  resource.summary = 'university/nhx8z14pae6nfk8vjif6',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611761/university/nhx8z14pae6nfk8vjif6.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Richmond, The American International University in London';

UPDATE resource
SET resource.homepage = 'https://www.roehampton.ac.uk',
  resource.handle = 'roehampton-university',
  resource.index_data = 'R513 U516',
  resource.quarter = 20174,
  resource.summary = 'university/iarwwipiqrpneoyzb4nd',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611762/university/iarwwipiqrpneoyzb4nd.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Roehampton University';

UPDATE resource
SET resource.homepage = 'https://www.bruford.ac.uk',
  resource.handle = 'rose-bruford-college-of',
  resource.index_data = 'R200 B616 C420 O100 S120 A530 D650',
  resource.quarter = 20174,
  resource.summary = 'university/tdc7owmmqmrmrppwmxrr',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611763/university/tdc7owmmqmrmrppwmxrr.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Rose Bruford College of Speech and Drama';

UPDATE resource
SET resource.homepage = 'https://www.rotherham.ac.uk',
  resource.handle = 'rotherham-college-of-arts',
  resource.index_data = 'R365 C420 O100 A632 A530 T254',
  resource.quarter = 20174,
  resource.summary = 'university/fw20mtye92rkteb2cfni',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611765/university/fw20mtye92rkteb2cfni.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Rotherham College of Arts and Technology';

UPDATE resource
SET resource.homepage = 'https://www.rad.org.uk',
  resource.handle = 'royal-academy-of-dance',
  resource.index_data = 'R400 A235 O100 D520',
  resource.quarter = 20174,
  resource.summary = 'university/cfb9bmhas5sgkzypnaxx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611766/university/cfb9bmhas5sgkzypnaxx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Royal Academy of Dance';

UPDATE resource
SET resource.homepage = 'https://www.ram.ac.uk',
  resource.handle = 'royal-academy-of-music',
  resource.index_data = 'R400 A235 O100 M220',
  resource.quarter = 20174,
  resource.summary = 'university/q7bximciv3pdtqlxzpou',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611768/university/q7bximciv3pdtqlxzpou.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Royal Academy of Music';

UPDATE resource
SET resource.homepage = 'https://www.rcm.ac.uk',
  resource.handle = 'royal-college-of-music',
  resource.index_data = 'R400 C420 O100 M220',
  resource.quarter = 20174,
  resource.summary = 'university/dkyv7yc8td9yiddundv5',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611769/university/dkyv7yc8td9yiddundv5.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Royal College of Music';

UPDATE resource
SET resource.homepage = 'https://www.rcs.ac.uk',
  resource.handle = 'royal-conservatoire-of',
  resource.index_data = 'R400 C526 O100 S345',
  resource.quarter = 20174,
  resource.summary = 'university/lwwlbxklcghmjwnu98i5',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611771/university/lwwlbxklcghmjwnu98i5.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Royal Conservatoire of Scotland';

UPDATE resource
SET resource.homepage = 'https://www.royalholloway.ac.uk',
  resource.handle = 'royal-holloway-college',
  resource.index_data = 'R400 H400 C420',
  resource.quarter = 20174,
  resource.summary = 'university/yv6t2kqoaicgsvymgb8j',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611772/university/yv6t2kqoaicgsvymgb8j.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Royal Holloway College';

UPDATE resource
SET resource.homepage = 'https://www.rncm.ac.uk',
  resource.handle = 'royal-northern-college-of',
  resource.index_data = 'R400 N636 C420 O100 M220',
  resource.quarter = 20174,
  resource.summary = 'university/cunahq97ahgl59vouybu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611773/university/cunahq97ahgl59vouybu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Royal Northern College of Music';

UPDATE resource
SET resource.homepage = 'https://www.rvc.ac.uk',
  resource.handle = 'royal-veterinary-college',
  resource.index_data = 'R400 V365 C420',
  resource.quarter = 20174,
  resource.summary = 'university/j5vex51803517evirw3x',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611774/university/j5vex51803517evirw3x.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Royal Veterinary College';

UPDATE resource
SET resource.homepage = 'https://www.rwcmd.ac.uk',
  resource.handle = 'royal-welsh-college-of',
  resource.index_data = 'R400 W420 C420 O100 M220 A530 D650',
  resource.quarter = 20174,
  resource.summary = 'university/o4wxosxh6rzt1e8moedf',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611775/university/o4wxosxh6rzt1e8moedf.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Royal Welsh College of Music and Drama';

UPDATE resource
SET resource.homepage = 'https://www.rwcmd.ac.uk',
  resource.handle = 'coleg-brenhinol-cerdd-a',
  resource.index_data = 'C420 B655 C630 D650 C560',
  resource.quarter = 20174,
  resource.summary = 'university/tq4rv9wkjiorbeqwveor',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611777/university/tq4rv9wkjiorbeqwveor.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Coleg Brenhinol Cerdd a Drama Cymru';

UPDATE resource
SET resource.homepage = 'https://www.runshaw.ac.uk',
  resource.handle = 'runshaw-adult-college',
  resource.index_data = 'R520 A343 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ptat2kqmldzxa0qmgiw8',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611777/university/ptat2kqmldzxa0qmgiw8.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Runshaw Adult College';

UPDATE resource
SET resource.homepage = 'https://www.ruskin.ac.uk',
  resource.handle = 'ruskin-college-oxford',
  resource.index_data = 'R250 C420 O216',
  resource.quarter = 20174,
  resource.summary = 'university/kdrcfi0wxha35edxpsed',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611778/university/kdrcfi0wxha35edxpsed.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Ruskin College Oxford';

UPDATE resource
SET resource.homepage = 'https://www.uk.sae.edu',
  resource.handle = 'sae-institute-oxford',
  resource.index_data = 'S000 I523 O216',
  resource.quarter = 20174,
  resource.summary = 'university/xkl4kyp6xreho5rsojid',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611782/university/xkl4kyp6xreho5rsojid.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'SAE Institute Oxford';

UPDATE resource
SET resource.homepage = 'https://www.salfordcc.ac.uk',
  resource.handle = 'salford-city-college',
  resource.index_data = 'S416 C300 C420',
  resource.quarter = 20174,
  resource.summary = 'university/cuivojb6rp1sk21a01gi',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611785/university/cuivojb6rp1sk21a01gi.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Salford City College';

UPDATE resource
SET resource.homepage = 'https://www.sandwell.ac.uk',
  resource.handle = 'sandwell-college',
  resource.index_data = 'S534 C420',
  resource.quarter = 20174,
  resource.summary = 'university/nw4wz02rnciwebetroh6',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611786/university/nw4wz02rnciwebetroh6.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Sandwell College';

UPDATE resource
SET resource.homepage = 'https://www.selby.ac.uk',
  resource.handle = 'selby-college',
  resource.index_data = 'S410 C420',
  resource.quarter = 20174,
  resource.summary = 'university/s3zcji8lrgai65cmqtn8',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611788/university/s3zcji8lrgai65cmqtn8.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Selby College';

UPDATE resource
SET resource.homepage = 'https://www.sheffcol.ac.uk',
  resource.handle = 'sheffield-college',
  resource.index_data = 'S143 C420',
  resource.quarter = 20174,
  resource.summary = 'university/xrslq7feuqexvnc6r2ev',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611789/university/xrslq7feuqexvnc6r2ev.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Sheffield College';

UPDATE resource
SET resource.homepage = 'https://www.shu.ac.uk',
  resource.handle = 'sheffield-hallam',
  resource.index_data = 'S143 H450 U516',
  resource.quarter = 20174,
  resource.summary = 'university/h0dhisdbwctdnzjnfzxz',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611790/university/h0dhisdbwctdnzjnfzxz.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Sheffield Hallam University';

UPDATE resource
SET resource.homepage = 'https://www.soas.ac.uk',
  resource.handle = 'university-of-london-2',
  resource.index_data = 'S200 U516 O100 L535',
  resource.quarter = 20174,
  resource.summary = 'university/vw7zdj0g3mqvhdelhinp',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611792/university/vw7zdj0g3mqvhdelhinp.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'SOAS, University of London';

UPDATE resource
SET resource.homepage = 'https://www.solihull.ac.uk',
  resource.handle = 'solihull-college',
  resource.index_data = 'S440 C420',
  resource.quarter = 20174,
  resource.summary = 'university/nd4kyifr5iefoxnvso6e',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611793/university/nd4kyifr5iefoxnvso6e.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Solihull College';

UPDATE resource
SET resource.homepage = 'https://www.somerset.ac.uk',
  resource.handle = 'somerset-college',
  resource.index_data = 'S562 C420',
  resource.quarter = 20174,
  resource.summary = 'university/j1y61nv0shldcgz8shhw',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611794/university/j1y61nv0shldcgz8shhw.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Somerset College';

UPDATE resource
SET resource.homepage = 'https://www.sccb.ac.uk',
  resource.handle = 'south-and-city-college',
  resource.index_data = 'S300 A530 C300 C420 B655',
  resource.quarter = 20174,
  resource.summary = 'university/p4oyik7o9xylfevyb7je',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611796/university/p4oyik7o9xylfevyb7je.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'South and City College Birmingham';

UPDATE resource
SET resource.homepage = 'https://www.s-cheshire.ac.uk',
  resource.handle = 'south-cheshire-college',
  resource.index_data = 'S300 C260 C420',
  resource.quarter = 20174,
  resource.summary = 'university/a6jpv1qptbyb2zjgzq5l',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611797/university/a6jpv1qptbyb2zjgzq5l.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'South Cheshire College';

UPDATE resource
SET resource.homepage = 'https://www.southdevon.ac.uk',
  resource.handle = 'south-devon-college',
  resource.index_data = 'S300 D150 C420',
  resource.quarter = 20174,
  resource.summary = 'university/jyc1fh57ashqaxuwcdio',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611798/university/jyc1fh57ashqaxuwcdio.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'South Devon College';

UPDATE resource
SET resource.homepage = 'https://www.southdowns.ac.uk',
  resource.handle = 'south-downs-college',
  resource.index_data = 'S300 D520 C420',
  resource.quarter = 20174,
  resource.summary = 'university/izxpoqke3uqhesfhrc69',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611801/university/izxpoqke3uqhesfhrc69.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'South Downs College';

UPDATE resource
SET resource.homepage = 'https://www.sgscol.ac.uk',
  resource.handle = 'south-gloucestershire-and',
  resource.index_data = 'S300 G422 A530 S363 C420',
  resource.quarter = 20174,
  resource.summary = 'university/zdwecsrwvce5xmslrpag',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611803/university/zdwecsrwvce5xmslrpag.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'South Gloucestershire and Stroud College';

UPDATE resource
SET resource.homepage = 'https://www.slcollege.ac.uk',
  resource.handle = 'south-leicestershire',
  resource.index_data = 'S300 L223 C420',
  resource.quarter = 20174,
  resource.summary = 'university/xix3tyloprieo0wqbrha',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611805/university/xix3tyloprieo0wqbrha.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'South Leicestershire College';

UPDATE resource
SET resource.homepage = 'https://www.south-thames.ac.uk',
  resource.handle = 'south-thames-college',
  resource.index_data = 'S300 T520 C420',
  resource.quarter = 20174,
  resource.summary = 'university/z0ijtx7lzffs08vk0s4j',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611806/university/z0ijtx7lzffs08vk0s4j.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'South Thames College';

UPDATE resource
SET resource.homepage = 'https://www.stc.ac.uk',
  resource.handle = 'south-tyneside-college',
  resource.index_data = 'S300 T523 C420',
  resource.quarter = 20174,
  resource.summary = 'university/abvnq4gsoo0n399b8owi',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611808/university/abvnq4gsoo0n399b8owi.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'South Tyneside College';

UPDATE resource
SET resource.homepage = 'https://www.solent.ac.uk',
  resource.handle = 'southampton-solent',
  resource.index_data = 'S351 S453 U516',
  resource.quarter = 20174,
  resource.summary = 'university/tt94cujfsmfxqeborlu3',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611809/university/tt94cujfsmfxqeborlu3.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Southampton Solent University';

UPDATE resource
SET resource.homepage = 'https://www.southport.ac.uk',
  resource.handle = 'southport-college',
  resource.index_data = 'S316 C420',
  resource.quarter = 20174,
  resource.summary = 'university/p3idxe9trxq9bs57bvvl',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611811/university/p3idxe9trxq9bs57bvvl.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Southport College';

UPDATE resource
SET resource.homepage = 'https://www.sparsholt.ac.uk',
  resource.handle = 'sparsholt-college',
  resource.index_data = 'S162 C420 H512',
  resource.quarter = 20174,
  resource.summary = 'university/yziw6myt1aatrlpyifko',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611813/university/yziw6myt1aatrlpyifko.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Sparsholt College Hampshire';

UPDATE resource
SET resource.homepage = 'https://www.spurgeons.ac.uk',
  resource.handle = 'spurgeons-college',
  resource.index_data = 'S162 C420',
  resource.quarter = 20174,
  resource.summary = 'university/frnplzt0ghtoojjbimdc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611813/university/frnplzt0ghtoojjbimdc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Spurgeons College';

UPDATE resource
SET resource.homepage = 'https://www.sruc.ac.uk',
  resource.handle = 'sruc-scotlands-rural',
  resource.index_data = 'S620 S345 R640 C420',
  resource.quarter = 20174,
  resource.summary = 'university/iievgzvny4v7javz9okm',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611815/university/iievgzvny4v7javz9okm.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'SRUC - Scotlands Rural College';

UPDATE resource
SET resource.homepage = 'https://www.sgul.ac.uk',
  resource.handle = 'st-university-of-london',
  resource.index_data = 'S300 G622 U516 O100 L535',
  resource.quarter = 20174,
  resource.summary = 'university/uwtsknhwsqncukumqinu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611816/university/uwtsknhwsqncukumqinu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'St Georges, University of London';

UPDATE resource
SET resource.homepage = 'https://www.sthelens.ac.uk',
  resource.handle = 'st-helens-college',
  resource.index_data = 'S300 H452 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ynbydtfiotkhsbzz5qvv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611818/university/ynbydtfiotkhsbzz5qvv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'St Helens College';

UPDATE resource
SET resource.homepage = 'https://www.stmarysblackburn.ac.uk',
  resource.handle = 'st-marys-blackburn',
  resource.index_data = 'S300 M620 C420 B421',
  resource.quarter = 20174,
  resource.summary = 'university/uolrtfjto9wgyq7diwda',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611820/university/uolrtfjto9wgyq7diwda.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'St Marys College, Blackburn';

UPDATE resource
SET resource.homepage = 'https://www.stmarys.ac.uk',
  resource.handle = 'st-marys-london',
  resource.index_data = 'S300 M620 U516 T255 L535',
  resource.quarter = 20174,
  resource.summary = 'university/zbhhhvbq86hasxo8ky6i',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611822/university/zbhhhvbq86hasxo8ky6i.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'St Marys University, Twickenham, London';

UPDATE resource
SET resource.homepage = 'https://www.st-patricks.ac.uk',
  resource.handle = 'st-patricks-london',
  resource.index_data = 'S300 P362 C420 L535',
  resource.quarter = 20174,
  resource.summary = 'university/csyarpznkcn9h8qa4qoh',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611823/university/csyarpznkcn9h8qa4qoh.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'St Patricks College, London';

UPDATE resource
SET resource.homepage = 'https://www.staffs.ac.uk',
  resource.handle = 'staffordshire-university',
  resource.index_data = 'S316 U516',
  resource.quarter = 20174,
  resource.summary = 'university/fcvhgx1avlauxompe1tk',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611824/university/fcvhgx1avlauxompe1tk.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Staffordshire University';

UPDATE resource
SET resource.homepage = 'https://www.stephensoncoll.ac.uk',
  resource.handle = 'stephenson-college',
  resource.index_data = 'S315 C420 C414',
  resource.quarter = 20174,
  resource.summary = 'university/uqxbzehgfwuwjvezt6nb',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611825/university/uqxbzehgfwuwjvezt6nb.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Stephenson College Coalville';

UPDATE resource
SET resource.homepage = 'https://www.stockport.ac.uk',
  resource.handle = 'stockport-college',
  resource.index_data = 'S321 C420',
  resource.quarter = 20174,
  resource.summary = 'university/jab9fxn9g4ufz4vrdwjv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611826/university/jab9fxn9g4ufz4vrdwjv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Stockport College';

UPDATE resource
SET resource.homepage = 'https://www.stran.ac.uk',
  resource.handle = 'stranmillis-university',
  resource.index_data = 'S365 U516 C420',
  resource.quarter = 20174,
  resource.summary = 'university/xcgp5zettc0simv3ce6m',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611829/university/xcgp5zettc0simv3ce6m.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Stranmillis University College';

UPDATE resource
SET resource.homepage = 'https://www.stratford.ac.uk',
  resource.handle = 'stratford-upon-avon',
  resource.index_data = 'S363 U150 A150 C420',
  resource.quarter = 20174,
  resource.summary = 'university/aohwpdkg9icy7geszjt0',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611830/university/aohwpdkg9icy7geszjt0.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Stratford upon Avon College';

UPDATE resource
SET resource.homepage = 'https://www.sussexcoast.ac.uk',
  resource.handle = 'sussex-coast-college',
  resource.index_data = 'S220 C230 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ukbzuoo1vhjtsab9euvy',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611831/university/ukbzuoo1vhjtsab9euvy.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Sussex Coast College';

UPDATE resource
SET resource.homepage = 'https://www.swansea.ac.uk',
  resource.handle = 'swansea-university',
  resource.index_data = 'S520 U516',
  resource.quarter = 20174,
  resource.summary = 'university/e5we1icbqbzfrzymch5a',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611833/university/e5we1icbqbzfrzymch5a.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Swansea University';

UPDATE resource
SET resource.homepage = 'https://www.tameside.ac.uk',
  resource.handle = 'tameside-college',
  resource.index_data = 'T523 C420',
  resource.quarter = 20174,
  resource.summary = 'university/nsmuaijy8bspeutmbmyu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611835/university/nsmuaijy8bspeutmbmyu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Tameside College';

UPDATE resource
SET resource.homepage = 'https://www.tees.ac.uk',
  resource.handle = 'teesside-university',
  resource.index_data = 'T230 U516',
  resource.quarter = 20174,
  resource.summary = 'university/hjviz2oaogw6avlyploh',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611836/university/hjviz2oaogw6avlyploh.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Teesside University';

UPDATE resource
SET resource.homepage = 'https://www.liv-coll.ac.uk',
  resource.handle = 'city-of-liverpool-college',
  resource.index_data = 'C300 O100 L161 C420',
  resource.quarter = 20174,
  resource.summary = 'university/qtvw5ifrzglqujbnc4i8',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611838/university/qtvw5ifrzglqujbnc4i8.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City of Liverpool College';

UPDATE resource
SET resource.homepage = 'https://www.cem.ac.uk',
  resource.handle = 'college-of-estate',
  resource.index_data = 'C420 O100 E233 M525',
  resource.quarter = 20174,
  resource.summary = 'university/d0ypjo8058xlgwtedqzd',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611839/university/d0ypjo8058xlgwtedqzd.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'College of Estate Management';

UPDATE resource
SET resource.homepage = 'https://www.conel.ac.uk',
  resource.handle = 'college-of-haringey',
  resource.index_data = 'C420 O100 H652',
  resource.quarter = 20174,
  resource.summary = 'university/chdfpqhpc3jzidyuu9qo',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611840/university/chdfpqhpc3jzidyuu9qo.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'College of Haringey';

UPDATE resource
SET resource.homepage = 'https://www.icr.ac.uk',
  resource.handle = 'institute-of-cancer',
  resource.index_data = 'I523 O100 C526 R262',
  resource.quarter = 20174,
  resource.summary = 'university/jrjvvvdrxpvfbstov8ra',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611842/university/jrjvvvdrxpvfbstov8ra.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Institute of Cancer Research';

UPDATE resource
SET resource.homepage = 'https://www.icmp.co.uk',
  resource.handle = 'institute-of-contemporary',
  resource.index_data = 'I523 O100 C535 M220 P616',
  resource.quarter = 20174,
  resource.summary = 'university/ztao0tz0yus5ayccegwx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611843/university/ztao0tz0yus5ayccegwx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Institute of Contemporary Music Performance';

UPDATE resource
SET resource.homepage = 'https://www.lipa.ac.uk',
  resource.handle = 'liverpool-institute-for',
  resource.index_data = 'L161 I523 F600 P616 A632',
  resource.quarter = 20174,
  resource.summary = 'university/wkenmdfqhhdy5dhxfmdc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611844/university/wkenmdfqhhdy5dhxfmdc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Liverpool Institute for Performing Arts';

UPDATE resource
SET resource.homepage = 'https://www.themanchestercollege.ac.uk',
  resource.handle = 'manchester-college',
  resource.index_data = 'M522 C420',
  resource.quarter = 20174,
  resource.summary = 'university/xnt2ohmebhmnmvqca4es',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611847/university/xnt2ohmebhmnmvqca4es.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Manchester College';

UPDATE resource
SET resource.homepage = 'https://www.mmu.ac.uk',
  resource.handle = 'manchester-metropolitan',
  resource.index_data = 'M522 M361 U516',
  resource.quarter = 20174,
  resource.summary = 'university/k38h34ff9t1cxn6k7uxe',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611848/university/k38h34ff9t1cxn6k7uxe.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Manchester Metropolitan University';

UPDATE resource
SET resource.homepage = 'https://www.rgu.ac.uk',
  resource.handle = 'robert-gordon-university',
  resource.index_data = 'R163 G635 U516',
  resource.quarter = 20174,
  resource.summary = 'university/yfwrinxxes8ad8hib9ka',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611850/university/yfwrinxxes8ad8hib9ka.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Robert Gordon University';

UPDATE resource
SET resource.homepage = 'https://www.rau.ac.uk',
  resource.handle = 'royal-agricultural',
  resource.index_data = 'R400 A262 U516',
  resource.quarter = 20174,
  resource.summary = 'university/ycrdoxksqeryrkvzrxms',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611851/university/ycrdoxksqeryrkvzrxms.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Royal Agricultural University';

UPDATE resource
SET resource.homepage = 'https://www.birmingham.ac.uk',
  resource.handle = 'university-of-birmingham',
  resource.index_data = 'U516 O100 B655',
  resource.quarter = 20174,
  resource.summary = 'university/ztwmi2dnmtqajcxb7ssa',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611853/university/ztwmi2dnmtqajcxb7ssa.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Birmingham';

UPDATE resource
SET resource.homepage = 'https://www.ed.ac.uk',
  resource.handle = 'university-of-edinburgh',
  resource.index_data = 'U516 O100 E351',
  resource.quarter = 20174,
  resource.summary = 'university/pqiso29q8v7oxjsqk6qy',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611854/university/pqiso29q8v7oxjsqk6qy.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Edinburgh';

UPDATE resource
SET resource.homepage = 'https://www.essex.ac.uk',
  resource.handle = 'university-of-essex',
  resource.index_data = 'U516 O100 E220',
  resource.quarter = 20174,
  resource.summary = 'university/e8i7sxrqu1wu5gxcaoxs',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611856/university/e8i7sxrqu1wu5gxcaoxs.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Essex';

UPDATE resource
SET resource.homepage = 'https://www.manchester.ac.uk',
  resource.handle = 'university-of-manchester',
  resource.index_data = 'U516 O100 M522',
  resource.quarter = 20174,
  resource.summary = 'university/z0eclkjdzkxrzpfx0sh3',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611858/university/z0eclkjdzkxrzpfx0sh3.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Manchester';

UPDATE resource
SET resource.homepage = 'https://www.salford.ac.uk',
  resource.handle = 'university-of-salford',
  resource.index_data = 'U516 O100 S416',
  resource.quarter = 20174,
  resource.summary = 'university/dbzgotb9iwgducxjjcva',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611859/university/dbzgotb9iwgducxjjcva.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Salford';

UPDATE resource
SET resource.homepage = 'https://www.strath.ac.uk',
  resource.handle = 'university-of-strathclyde',
  resource.index_data = 'U516 O100 S363',
  resource.quarter = 20174,
  resource.summary = 'university/yvbxia4xxpigaah7dnku',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611861/university/yvbxia4xxpigaah7dnku.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Strathclyde';

UPDATE resource
SET resource.homepage = 'https://www.uwl.ac.uk',
  resource.handle = 'university-of-west-london',
  resource.index_data = 'U516 O100 W230 L535',
  resource.quarter = 20174,
  resource.summary = 'university/xp0ykn7xykbmifueefx9',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611864/university/xp0ykn7xykbmifueefx9.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of West London';

UPDATE resource
SET resource.homepage = 'https://www.york.ac.uk',
  resource.handle = 'university-of-york',
  resource.index_data = 'U516 O100 Y620',
  resource.quarter = 20174,
  resource.summary = 'university/ras29jhbgdv2umrmekuz',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611865/university/ras29jhbgdv2umrmekuz.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of York';

UPDATE resource
SET resource.homepage = 'https://www.tottenhamhotspur.com',
  resource.handle = 'tottenham-hotspur',
  resource.index_data = 'T355 H321 F533',
  resource.quarter = 20174,
  resource.summary = 'university/e1zload1isse8tguany1',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611867/university/e1zload1isse8tguany1.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Tottenham Hotspur Foundation';

UPDATE resource
SET resource.homepage = 'https://www.tresham.ac.uk',
  resource.handle = 'tresham-college-of',
  resource.index_data = 'T625 C420 O100 F636 A530 H260 E323',
  resource.quarter = 20174,
  resource.summary = 'university/or6l9axh0deo4nwzpjj2',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611868/university/or6l9axh0deo4nwzpjj2.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Tresham College of Further and Higher Education';

UPDATE resource
SET resource.homepage = 'https://www.trinitylaban.ac.uk',
  resource.handle = 'trinity-laban',
  resource.index_data = 'T653 L150 C526 O100 M220 A530 D520',
  resource.quarter = 20174,
  resource.summary = 'university/xqipflec3bursyurxheg',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611869/university/xqipflec3bursyurxheg.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Trinity Laban Conservatoire of Music and Dance';

UPDATE resource
SET resource.homepage = 'https://www.truro-penwith.ac.uk',
  resource.handle = 'truro-and-penwith-college',
  resource.index_data = 'T660 A530 P530 C420',
  resource.quarter = 20174,
  resource.summary = 'university/sxrtif7sv60vb6xtiawh',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611870/university/sxrtif7sv60vb6xtiawh.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Truro and Penwith College';

UPDATE resource
SET resource.homepage = 'https://www.tynemet.ac.uk',
  resource.handle = 'tyne-metropolitan-college',
  resource.index_data = 'T500 M361 C420',
  resource.quarter = 20174,
  resource.summary = 'university/h1demzdkkd7pevs0nprs',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611872/university/h1demzdkkd7pevs0nprs.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Tyne Metropolitan College';

UPDATE resource
SET resource.homepage = 'https://www.ioe.ac.uk',
  resource.handle = 'ucl-institute-of',
  resource.index_data = 'U240 I523 O100 E323',
  resource.quarter = 20174,
  resource.summary = 'university/ljghniio92xyprdws4xb',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611873/university/ljghniio92xyprdws4xb.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'UCL Institute of Education';

UPDATE resource
SET resource.homepage = 'https://www.ulster.ac.uk',
  resource.handle = 'ulster-university',
  resource.index_data = 'U423 U516',
  resource.quarter = 20174,
  resource.summary = 'university/ru3huf2uxyynreaaz0h1',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611874/university/ru3huf2uxyynreaaz0h1.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Ulster University';

UPDATE resource
SET resource.homepage = 'https://www.uco.oldham.ac.uk',
  resource.handle = 'university-campus-oldham',
  resource.index_data = 'U516 C512 O435',
  resource.quarter = 20174,
  resource.summary = 'university/y6zs4t0veig69anppdsm',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611875/university/y6zs4t0veig69anppdsm.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University Campus Oldham';

UPDATE resource
SET resource.homepage = 'https://www.coventry.ac.uk/',
  resource.handle = 'university-campus',
  resource.index_data = 'U516 C512 S616',
  resource.quarter = 20174,
  resource.summary = 'university/iqarm22trrv80qxlc85e',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611876/university/iqarm22trrv80qxlc85e.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University Campus Scarborough';

UPDATE resource
SET resource.homepage = 'https://www.ucs.ac.uk',
  resource.handle = 'university-campus-suffolk',
  resource.index_data = 'U516 C512 S142',
  resource.quarter = 20174,
  resource.summary = 'university/k07dtctwjmkbtebcn0hd',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611878/university/k07dtctwjmkbtebcn0hd.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University Campus Suffolk';

UPDATE resource
SET resource.homepage = 'https://www.farn-ct.ac.uk',
  resource.handle = 'university-centre',
  resource.index_data = 'U516 C536 F651',
  resource.quarter = 20174,
  resource.summary = 'university/rcj582g7kppgiruwbdnn',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611881/university/rcj582g7kppgiruwbdnn.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University Centre Farnborough';

UPDATE resource
SET resource.homepage = 'https://www.grimsby.ac.uk',
  resource.handle = 'university-centre-grimsby',
  resource.index_data = 'U516 C536 G652',
  resource.quarter = 20174,
  resource.summary = 'university/wkjbghijsxej9locambi',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611883/university/wkjbghijsxej9locambi.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University Centre Grimsby';

UPDATE resource
SET resource.homepage = 'https://www.ucp.ac.uk/',
  resource.handle = 'university-centre-2',
  resource.index_data = 'U516 C536 P361',
  resource.quarter = 20174,
  resource.summary = 'university/tbjywox7zanfhfy839bz',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611884/university/tbjywox7zanfhfy839bz.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University Centre Peterborough';

UPDATE resource
SET resource.homepage = 'https://www.croydon.ac.uk',
  resource.handle = 'university-croydon',
  resource.index_data = 'U516 C536 C635',
  resource.quarter = 20174,
  resource.summary = 'university/vhqwuesbt5dnt6nidtba',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611886/university/vhqwuesbt5dnt6nidtba.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University Centre, Croydon';

UPDATE resource
SET resource.homepage = 'https://www.ucb.ac.uk',
  resource.handle = 'university-college',
  resource.index_data = 'U516 C420 B655',
  resource.quarter = 20174,
  resource.summary = 'university/wbdqmjevfuui6jwpxibu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611887/university/wbdqmjevfuui6jwpxibu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University College Birmingham';

UPDATE resource
SET resource.homepage = 'https://www.ucreative.ac.uk',
  resource.handle = 'university-for-the',
  resource.index_data = 'U516 F600 T000 C631 A632',
  resource.quarter = 20174,
  resource.summary = 'university/aol95tfrs9v2in8mtjxm',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611889/university/aol95tfrs9v2in8mtjxm.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University for the Creative Arts';

UPDATE resource
SET resource.homepage = 'https://www.bath.ac.uk',
  resource.handle = 'university-of-bath',
  resource.index_data = 'U516 O100 B300',
  resource.quarter = 20174,
  resource.summary = 'university/voeb0psivvdmpzrlkjud',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611890/university/voeb0psivvdmpzrlkjud.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Bath';

UPDATE resource
SET resource.homepage = 'https://www.bolton.ac.uk',
  resource.handle = 'university-of-bolton',
  resource.index_data = 'U516 O100 B435',
  resource.quarter = 20174,
  resource.summary = 'university/u3kwryjvxwqpg49fky93',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611891/university/u3kwryjvxwqpg49fky93.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Bolton';

UPDATE resource
SET resource.homepage = 'https://www.bradford.ac.uk',
  resource.handle = 'university-of-bradford',
  resource.index_data = 'U516 O100 B631',
  resource.quarter = 20174,
  resource.summary = 'university/ffmynogvvm9pgzxzehe8',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611893/university/ffmynogvvm9pgzxzehe8.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Bradford';

UPDATE resource
SET resource.homepage = 'https://www.bristol.ac.uk',
  resource.handle = 'university-of-bristol',
  resource.index_data = 'U516 O100 B623',
  resource.quarter = 20174,
  resource.summary = 'university/rbi2vesgu225pmvqy06q',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611894/university/rbi2vesgu225pmvqy06q.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Bristol';

UPDATE resource
SET resource.homepage = 'https://www.buckingham.ac.uk',
  resource.handle = 'university-of-buckingham',
  resource.index_data = 'U516 O100 B252',
  resource.quarter = 20174,
  resource.summary = 'university/iqeicipkowpitlqsbivn',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611896/university/iqeicipkowpitlqsbivn.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Buckingham';

UPDATE resource
SET resource.homepage = 'https://www.study.cam.ac.uk',
  resource.handle = 'university-of-cambridge',
  resource.index_data = 'U516 O100 C516',
  resource.quarter = 20174,
  resource.summary = 'university/rdjqjqbj5vrgtzyytjtv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611897/university/rdjqjqbj5vrgtzyytjtv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Cambridge';

UPDATE resource
SET resource.homepage = 'https://www.uclan.ac.uk',
  resource.handle = 'university-of-central',
  resource.index_data = 'U516 O100 C536 L522',
  resource.quarter = 20174,
  resource.summary = 'university/xaoeddbchicebt24zpsg',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611898/university/xaoeddbchicebt24zpsg.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Central Lancashire';

UPDATE resource
SET resource.homepage = 'https://www.chi.ac.uk',
  resource.handle = 'university-of-chichester',
  resource.index_data = 'U516 O100 C223',
  resource.quarter = 20174,
  resource.summary = 'university/eiecbgl6letdcj1dsqr1',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611899/university/eiecbgl6letdcj1dsqr1.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Chichester';

UPDATE resource
SET resource.homepage = 'https://www.cumbria.ac.uk',
  resource.handle = 'university-of-cumbria',
  resource.index_data = 'U516 O100 C516',
  resource.quarter = 20174,
  resource.summary = 'university/ttjlrnmharntrgxou7oq',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611900/university/ttjlrnmharntrgxou7oq.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Cumbria';

UPDATE resource
SET resource.homepage = 'https://www.derby.ac.uk',
  resource.handle = 'university-of-derby',
  resource.index_data = 'U516 O100 D610',
  resource.quarter = 20174,
  resource.summary = 'university/k0tjffjldkxmrldkytbk',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611902/university/k0tjffjldkxmrldkytbk.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Derby';

UPDATE resource
SET resource.homepage = 'https://www.dundee.ac.uk',
  resource.handle = 'university-of-dundee',
  resource.index_data = 'U516 O100 D530',
  resource.quarter = 20174,
  resource.summary = 'university/stbf8pnev4avxs5fkj37',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611903/university/stbf8pnev4avxs5fkj37.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Dundee';

UPDATE resource
SET resource.homepage = 'https://www.uea.ac.uk',
  resource.handle = 'university-of-east-anglia',
  resource.index_data = 'U516 O100 E230 A524',
  resource.quarter = 20174,
  resource.summary = 'university/vhwmgehleyceflgjavey',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611905/university/vhwmgehleyceflgjavey.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of East Anglia';

UPDATE resource
SET resource.homepage = 'https://www.uel.ac.uk',
  resource.handle = 'university-of-east-london',
  resource.index_data = 'U516 O100 E230 L535',
  resource.quarter = 20174,
  resource.summary = 'university/itbweunvo3ntnj0ldysu',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611906/university/itbweunvo3ntnj0ldysu.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of East London';

UPDATE resource
SET resource.homepage = 'https://www.exeter.ac.uk',
  resource.handle = 'university-of-exeter',
  resource.index_data = 'U516 O100 E236',
  resource.quarter = 20174,
  resource.summary = 'university/vqmnygfwxp0eacmnl3ui',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611907/university/vqmnygfwxp0eacmnl3ui.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Exeter';

UPDATE resource
SET resource.homepage = 'https://www.glasgow.ac.uk',
  resource.handle = 'university-of-glasgow',
  resource.index_data = 'U516 O100 G420',
  resource.quarter = 20174,
  resource.summary = 'university/zgm8hhpkxcdtljy48ty1',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611909/university/zgm8hhpkxcdtljy48ty1.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Glasgow';

UPDATE resource
SET resource.homepage = 'https://www.glos.ac.uk',
  resource.handle = 'university-of',
  resource.index_data = 'U516 O100 G422',
  resource.quarter = 20174,
  resource.summary = 'university/gv582vgaelxtdytirvrg',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611912/university/gv582vgaelxtdytirvrg.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Gloucestershire';

UPDATE resource
SET resource.homepage = 'https://www.gre.ac.uk',
  resource.handle = 'university-of-greenwich',
  resource.index_data = 'U516 O100 G652',
  resource.quarter = 20174,
  resource.summary = 'university/yhw61zi6rebg4ptxcxai',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611916/university/yhw61zi6rebg4ptxcxai.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Greenwich';

UPDATE resource
SET resource.homepage = 'https://www.herts.ac.uk',
  resource.handle = 'university-of-2',
  resource.index_data = 'U516 O100 H631',
  resource.quarter = 20174,
  resource.summary = 'university/xaiagchm2es7qpfyzx6p',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611917/university/xaiagchm2es7qpfyzx6p.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Hertfordshire';

UPDATE resource
SET resource.homepage = 'https://www.hull.ac.uk',
  resource.handle = 'university-of-hull',
  resource.index_data = 'U516 O100 H400',
  resource.quarter = 20174,
  resource.summary = 'university/kxnniqji0yuq4lwtzulo',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611920/university/kxnniqji0yuq4lwtzulo.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Hull';

UPDATE resource
SET resource.homepage = 'https://www.kent.ac.uk',
  resource.handle = 'university-of-kent',
  resource.index_data = 'U516 O100 K530',
  resource.quarter = 20174,
  resource.summary = 'university/vlpmruqnuv2guqpksscw',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611921/university/vlpmruqnuv2guqpksscw.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Kent';

UPDATE resource
SET resource.homepage = 'https://www.law.ac.uk',
  resource.handle = 'university-of-law',
  resource.index_data = 'U516 O100 L000',
  resource.quarter = 20174,
  resource.summary = 'university/ttytsw5zpdkdmbzeeigr',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611923/university/ttytsw5zpdkdmbzeeigr.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Law';

UPDATE resource
SET resource.homepage = 'https://www.leeds.ac.uk',
  resource.handle = 'university-of-leeds',
  resource.index_data = 'U516 O100 L320',
  resource.quarter = 20174,
  resource.summary = 'university/eo3292jqmyeukkyusei4',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611925/university/eo3292jqmyeukkyusei4.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Leeds';

UPDATE resource
SET resource.homepage = 'https://www.le.ac.uk',
  resource.handle = 'university-of-leicester',
  resource.index_data = 'U516 O100 L223',
  resource.quarter = 20174,
  resource.summary = 'university/rcvzsyixwaolxg8olqmq',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611926/university/rcvzsyixwaolxg8olqmq.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Leicester';

UPDATE resource
SET resource.homepage = 'https://www.lincoln.ac.uk',
  resource.handle = 'university-of-lincoln',
  resource.index_data = 'U516 O100 L524',
  resource.quarter = 20174,
  resource.summary = 'university/vrqo86pzno96llhpvm2c',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611928/university/vrqo86pzno96llhpvm2c.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Lincoln';

UPDATE resource
SET resource.homepage = 'https://www.liv.ac.uk',
  resource.handle = 'university-of-liverpool',
  resource.index_data = 'U516 O100 L161',
  resource.quarter = 20174,
  resource.summary = 'university/zr8fchtmxndfxknwwuvz',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611929/university/zr8fchtmxndfxknwwuvz.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Liverpool';

UPDATE resource
SET resource.homepage = 'https://www.ulip.london.ac.uk',
  resource.handle = 'university-of-london-3',
  resource.index_data = 'U516 O100 L535 I523 I500 P620',
  resource.quarter = 20174,
  resource.summary = 'university/b2bsxeglb52xhxaijm4e',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611931/university/b2bsxeglb52xhxaijm4e.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of London Institute in Paris';

UPDATE resource
SET resource.homepage = 'https://www.ncl.ac.uk',
  resource.handle = 'university-of-newcastle',
  resource.index_data = 'U516 O100 N223',
  resource.quarter = 20174,
  resource.summary = 'university/plxfvoktiwe4kb350uyq',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611933/university/plxfvoktiwe4kb350uyq.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Newcastle';

UPDATE resource
SET resource.homepage = 'https://www.northampton.ac.uk',
  resource.handle = 'university-of-northampton',
  resource.index_data = 'U516 O100 N635',
  resource.quarter = 20174,
  resource.summary = 'university/jxzw5pkruuyqigwbtoyo',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611935/university/jxzw5pkruuyqigwbtoyo.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Northampton';

UPDATE resource
SET resource.homepage = 'https://www.nottingham.ac.uk',
  resource.handle = 'university-of-nottingham',
  resource.index_data = 'U516 O100 N352',
  resource.quarter = 20174,
  resource.summary = 'university/b16u9hvbr1fzymqohe1r',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611937/university/b16u9hvbr1fzymqohe1r.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Nottingham';

UPDATE resource
SET resource.homepage = 'https://www.ox.ac.uk',
  resource.handle = 'university-of-oxford',
  resource.index_data = 'U516 O100 O216',
  resource.quarter = 20174,
  resource.summary = 'university/gv0rmruig1sg3oyjtjbc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611939/university/gv0rmruig1sg3oyjtjbc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Oxford';

UPDATE resource
SET resource.homepage = 'https://www.port.ac.uk',
  resource.handle = 'university-of-portsmouth',
  resource.index_data = 'U516 O100 P632',
  resource.quarter = 20174,
  resource.summary = 'university/iyxj3dcjixijmfvn2egp',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611941/university/iyxj3dcjixijmfvn2egp.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Portsmouth';

UPDATE resource
SET resource.homepage = 'https://www.reading.ac.uk',
  resource.handle = 'university-of-reading',
  resource.index_data = 'U516 O100 R352',
  resource.quarter = 20174,
  resource.summary = 'university/cupc6oqihfrrejukeoq4',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611942/university/cupc6oqihfrrejukeoq4.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Reading';

UPDATE resource
SET resource.homepage = 'https://www.sheffield.ac.uk',
  resource.handle = 'university-of-sheffield',
  resource.index_data = 'U516 O100 S143',
  resource.quarter = 20174,
  resource.summary = 'university/ljkru4vtsizcxgak6fzy',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611945/university/ljkru4vtsizcxgak6fzy.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Sheffield';

UPDATE resource
SET resource.homepage = 'https://www.southwales.ac.uk',
  resource.handle = 'university-of-south-wales',
  resource.index_data = 'U516 O100 S300 W420',
  resource.quarter = 20174,
  resource.summary = 'university/nswtbdhdoarsc5iqxjuc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611948/university/nswtbdhdoarsc5iqxjuc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of South Wales';

UPDATE resource
SET resource.homepage = 'https://www.southampton.ac.uk',
  resource.handle = 'university-of-southampton',
  resource.index_data = 'U516 O100 S351',
  resource.quarter = 20174,
  resource.summary = 'university/vwqyvn7zzdqarsafg8sd',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611952/university/vwqyvn7zzdqarsafg8sd.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Southampton';

UPDATE resource
SET resource.homepage = 'https://www.st-andrews.ac.uk',
  resource.handle = 'university-of-st-andrews',
  resource.index_data = 'U516 O100 S300 A536',
  resource.quarter = 20174,
  resource.summary = 'university/ep65lryfrndboymaf7es',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611953/university/ep65lryfrndboymaf7es.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of St Andrews';

UPDATE resource
SET resource.homepage = 'https://www.stir.ac.uk',
  resource.handle = 'university-of-stirling',
  resource.index_data = 'U516 O100 S364',
  resource.quarter = 20174,
  resource.summary = 'university/ulw7nawb1sk9pfvjfb13',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611956/university/ulw7nawb1sk9pfvjfb13.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Stirling';

UPDATE resource
SET resource.homepage = 'https://www.sunderland.ac.uk',
  resource.handle = 'university-of-sunderland',
  resource.index_data = 'U516 O100 S536',
  resource.quarter = 20174,
  resource.summary = 'university/n5kxoxqyw55itbbe60rc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611958/university/n5kxoxqyw55itbbe60rc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Sunderland';

UPDATE resource
SET resource.homepage = 'https://www.surrey.ac.uk',
  resource.handle = 'university-of-surrey',
  resource.index_data = 'U516 O100 S600',
  resource.quarter = 20174,
  resource.summary = 'university/rw9kab2ze7loimtiyeqx',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611960/university/rw9kab2ze7loimtiyeqx.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Surrey';

UPDATE resource
SET resource.homepage = 'https://www.sussex.ac.uk',
  resource.handle = 'university-of-sussex',
  resource.index_data = 'U516 O100 S220',
  resource.quarter = 20174,
  resource.summary = 'university/mdt8f0msfieplrzpcvtk',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611962/university/mdt8f0msfieplrzpcvtk.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Sussex';

UPDATE resource
SET resource.homepage = 'https://www.arts.ac.uk',
  resource.handle = 'university-of-the-london',
  resource.index_data = 'U516 O100 T000 A632 L535',
  resource.quarter = 20174,
  resource.summary = 'university/bd1q8oiri2yjkskdigbq',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611964/university/bd1q8oiri2yjkskdigbq.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of the Arts, London';

UPDATE resource
SET resource.homepage = 'https://www.uhi.ac.uk',
  resource.handle = 'university-of-the',
  resource.index_data = 'U516 O100 T000 H245 A530 I245',
  resource.quarter = 20174,
  resource.summary = 'university/l0mqcnegrnyandidican',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611966/university/l0mqcnegrnyandidican.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of the Highlands and Islands';

UPDATE resource
SET resource.homepage = 'https://www.uws.ac.uk',
  resource.handle = 'university-of-the-west-of',
  resource.index_data = 'U516 O100 T000 W230 O100 S345',
  resource.quarter = 20174,
  resource.summary = 'university/twqfw9x3tfkqciz8uhrb',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611968/university/twqfw9x3tfkqciz8uhrb.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of the West of Scotland';

UPDATE resource
SET resource.homepage = 'https://www.uwtsd.ac.uk',
  resource.handle = 'university-of-wales',
  resource.index_data = 'U516 O100 W420 T653 S530 D130',
  resource.quarter = 20174,
  resource.summary = 'university/iw9z8vvptvjuncv1fht9',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611969/university/iw9z8vvptvjuncv1fht9.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Wales Trinity Saint David';

UPDATE resource
SET resource.homepage = 'https://www.warwick.ac.uk',
  resource.handle = 'university-of-warwick',
  resource.index_data = 'U516 O100 W620',
  resource.quarter = 20174,
  resource.summary = 'university/ahtdsneiu2sb6c2dbup4',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611971/university/ahtdsneiu2sb6c2dbup4.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Warwick';

UPDATE resource
SET resource.homepage = 'https://www.westminster.ac.uk',
  resource.handle = 'university-of-westminster',
  resource.index_data = 'U516 O100 W235',
  resource.quarter = 20174,
  resource.summary = 'university/cduskg7tzhu8x5vyunkl',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611973/university/cduskg7tzhu8x5vyunkl.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Westminster';

UPDATE resource
SET resource.homepage = 'https://www.wlv.ac.uk',
  resource.handle = 'university-of-4',
  resource.index_data = 'U516 O100 W416',
  resource.quarter = 20174,
  resource.summary = 'university/sfxyqni0oxv8avqkma6v',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611975/university/sfxyqni0oxv8avqkma6v.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Wolverhampton';

UPDATE resource
SET resource.homepage = 'https://www.worcester.ac.uk',
  resource.handle = 'university-of-worcester',
  resource.index_data = 'U516 O100 W622',
  resource.quarter = 20174,
  resource.summary = 'university/iihu18kjilmtlsdcchn4',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611976/university/iihu18kjilmtlsdcchn4.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Worcester';

UPDATE resource
SET resource.homepage = 'https://www.uxbridgecollege.ac.uk',
  resource.handle = 'uxbridge-college',
  resource.index_data = 'U216 C420',
  resource.quarter = 20174,
  resource.summary = 'university/irl80enndtu4mdbwc6ct',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611978/university/irl80enndtu4mdbwc6ct.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Uxbridge College';

UPDATE resource
SET resource.homepage = 'https://www.wakefield.ac.uk',
  resource.handle = 'wakefield-college',
  resource.index_data = 'W214 C420',
  resource.quarter = 20174,
  resource.summary = 'university/kf6gmsyrzuwxvnw54c6f',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611980/university/kf6gmsyrzuwxvnw54c6f.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Wakefield College';

UPDATE resource
SET resource.homepage = 'https://www.walsallcollege.ac.uk',
  resource.handle = 'walsall-college',
  resource.index_data = 'W424 C420',
  resource.quarter = 20174,
  resource.summary = 'university/bux3ocncudlsi2seqp1e',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611982/university/bux3ocncudlsi2seqp1e.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Walsall College';

UPDATE resource
SET resource.homepage = 'https://www.west-cheshire.ac.uk',
  resource.handle = 'west-cheshire-college',
  resource.index_data = 'W230 C260 C420',
  resource.quarter = 20174,
  resource.summary = 'university/h5mvow9zc8d7mjp0zgzv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611985/university/h5mvow9zc8d7mjp0zgzv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'West Cheshire College';

UPDATE resource
SET resource.homepage = 'https://www.westherts.ac.uk',
  resource.handle = 'west-herts-college',
  resource.index_data = 'W230 H632 C420',
  resource.quarter = 20174,
  resource.summary = 'university/adgojvpkaeebbkuhvm0z',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611987/university/adgojvpkaeebbkuhvm0z.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'West Herts College';

UPDATE resource
SET resource.homepage = 'https://www.westkent.ac.uk',
  resource.handle = 'west-kent-and-ashford',
  resource.index_data = 'W230 K530 A530 A216 C420',
  resource.quarter = 20174,
  resource.summary = 'university/g45rtgzyzvdzce71phtg',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611989/university/g45rtgzyzvdzce71phtg.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'West Kent and Ashford College';

UPDATE resource
SET resource.homepage = 'https://www.westlancs.ac.uk',
  resource.handle = 'west-lancashire-college',
  resource.index_data = 'W230 L522 C420',
  resource.quarter = 20174,
  resource.summary = 'university/mplx1g42lsgcitjmi5gt',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611990/university/mplx1g42lsgcitjmi5gt.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'West Lancashire College';

UPDATE resource
SET resource.homepage = 'https://www.west-thames.ac.uk',
  resource.handle = 'west-thames-college',
  resource.index_data = 'W230 T520 C420',
  resource.quarter = 20174,
  resource.summary = 'university/dotudrfe2tallimutttc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611992/university/dotudrfe2tallimutttc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'West Thames College';

UPDATE resource
SET resource.homepage = 'https://www.westking.ac.uk',
  resource.handle = 'westminster-kingsway',
  resource.index_data = 'W235 K520 C420',
  resource.quarter = 20174,
  resource.summary = 'university/agvmxlwpdcezoclsj13e',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611994/university/agvmxlwpdcezoclsj13e.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Westminster Kingsway College';

UPDATE resource
SET resource.homepage = 'https://www.weston.ac.uk',
  resource.handle = 'weston-college',
  resource.index_data = 'W235 C420',
  resource.quarter = 20174,
  resource.summary = 'university/itons9tqdfkwceqyhpo5',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611995/university/itons9tqdfkwceqyhpo5.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Weston College';

UPDATE resource
SET resource.homepage = 'https://www.weymouth.ac.uk',
  resource.handle = 'weymouth-college',
  resource.index_data = 'W530 C420',
  resource.quarter = 20174,
  resource.summary = 'university/vh9lrihhqvgobb9gdlgy',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611996/university/vh9lrihhqvgobb9gdlgy.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Weymouth College';

UPDATE resource
SET resource.homepage = 'https://www.wigan-leigh.ac.uk',
  resource.handle = 'wigan-and-leigh-college',
  resource.index_data = 'W250 A530 L200 C420',
  resource.quarter = 20174,
  resource.summary = 'university/e2hk5xurnvf4y2fsktkj',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510611998/university/e2hk5xurnvf4y2fsktkj.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Wigan and Leigh College';

UPDATE resource
SET resource.homepage = 'https://www.wiltshire.ac.uk',
  resource.handle = 'wiltshire-college',
  resource.index_data = 'W432 C420',
  resource.quarter = 20174,
  resource.summary = 'university/vqif0bkisrcjjf0ybafv',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510612000/university/vqif0bkisrcjjf0ybafv.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Wiltshire College';

UPDATE resource
SET resource.homepage = 'https://www.wmc.ac.uk',
  resource.handle = 'wirral-metropolitan',
  resource.index_data = 'W640 M361 C420',
  resource.quarter = 20174,
  resource.summary = 'university/rtgjc9w75awettcjzjsc',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510612001/university/rtgjc9w75awettcjzjsc.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Wirral Metropolitan College';

UPDATE resource
SET resource.homepage = 'https://www.wortech.ac.uk',
  resource.handle = 'worcester-college-of',
  resource.index_data = 'W622 C420 O100 T254',
  resource.quarter = 20174,
  resource.summary = 'university/qgddtpfejgfncpqlplk4',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510612002/university/qgddtpfejgfncpqlplk4.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Worcester College of Technology';

UPDATE resource
SET resource.homepage = 'https://www.writtle.ac.uk',
  resource.handle = 'writtle-college',
  resource.index_data = 'W634 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ndylrbfny5bgw8zzillg',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510612004/university/ndylrbfny5bgw8zzillg.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Writtle College';

UPDATE resource
SET resource.homepage = 'https://www.yorkcollege.ac.uk',
  resource.handle = 'york-college',
  resource.index_data = 'Y620 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ohsvbnwot1rromfpdleq',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510612006/university/ohsvbnwot1rromfpdleq.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'York College';

UPDATE resource
SET resource.homepage = 'https://www.yorkshirecoastcollege.ac.uk',
  resource.handle = 'yorkshire-coast-college',
  resource.index_data = 'Y626 C230 C420',
  resource.quarter = 20174,
  resource.summary = 'university/ayaonm0vcusie6urlnys',
  resource.description = 'https://res.cloudinary.com/board-prism-hr/image/upload/v1510612008/university/ayaonm0vcusie6urlnys.png'
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Yorkshire Coast College';

UPDATE resource
SET resource.homepage = 'https://www.bucks.ac.uk',
  resource.handle = 'buckinghamshire-new',
  resource.index_data = 'B252 N000 U516',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Buckinghamshire New University';

UPDATE resource
SET resource.homepage = 'https://www.chichester.ac.uk',
  resource.handle = 'chichester-college',
  resource.index_data = 'C223 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Chichester College';

UPDATE resource
SET resource.homepage = 'https://www.ccb.ac.uk',
  resource.handle = 'city-college-brighton-and',
  resource.index_data = 'C300 C420 B623 A530 H100',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'City College Brighton and Hove';

UPDATE resource
SET resource.homepage = 'https://www.menai.ac.uk',
  resource.handle = 'coleg-menai',
  resource.index_data = 'C420 M500',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Coleg Menai';

UPDATE resource
SET resource.homepage = 'https://www.craven-college.ac.uk',
  resource.handle = 'craven-college',
  resource.index_data = 'C615 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Craven College';

UPDATE resource
SET resource.homepage = 'https://www.creativeacademy.org',
  resource.handle = 'creative-academy',
  resource.index_data = 'C631 A235',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Creative Academy';

UPDATE resource
SET resource.homepage = 'https://www.derby-college.ac.uk',
  resource.handle = 'derby-college',
  resource.index_data = 'D610 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Derby College';

UPDATE resource
SET resource.homepage = 'https://www.eastonotley.ac.uk',
  resource.handle = 'easton-and-otley-college',
  resource.index_data = 'E235 A530 O340 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Easton and Otley College';

UPDATE resource
SET resource.homepage = 'https://www.ebslondon.ac.uk',
  resource.handle = 'european-business-london',
  resource.index_data = 'E615 B252 S400 L535',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'European Business School, London';

UPDATE resource
SET resource.homepage = 'https://www.eselondon.ac.uk',
  resource.handle = 'european-school-of',
  resource.index_data = 'E615 S400 O100 E255',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'European School of Economics';

UPDATE resource
SET resource.homepage = 'https://www.guildford.ac.uk',
  resource.handle = 'guildford-college',
  resource.index_data = 'G431 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Guildford College';

UPDATE resource
SET resource.homepage = 'https://www.havering-college.ac.uk',
  resource.handle = 'havering-college-of',
  resource.index_data = 'H165 C420 O100 F636 A530 H260 E323',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Havering College of Further and Higher Education';

UPDATE resource
SET resource.homepage = 'https://www.hughbaird.ac.uk',
  resource.handle = 'hugh-baird-college',
  resource.index_data = 'H200 B630 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Hugh Baird College';

UPDATE resource
SET resource.homepage = 'https://www.lcwc.ac.uk',
  resource.handle = 'lakes-college-west',
  resource.index_data = 'L220 C420 W230 C516',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Lakes College - West Cumbria';

UPDATE resource
SET resource.homepage = 'https://www.lec.org.uk',
  resource.handle = 'london-electronics',
  resource.index_data = 'L535 E423 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London Electronics College';

UPDATE resource
SET resource.homepage = 'https://www.midchesh.ac.uk',
  resource.handle = 'mid-cheshire-college',
  resource.index_data = 'M300 C260 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Mid Cheshire College';

UPDATE resource
SET resource.homepage = 'https://www.ncn.ac.uk',
  resource.handle = 'new-college-nottingham',
  resource.index_data = 'N000 C420 N352',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'New College Nottingham';

UPDATE resource
SET resource.homepage = 'https://www.parkroyalcollege.org',
  resource.handle = 'park-royal-college',
  resource.index_data = 'P620 R400 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Park Royal College';

UPDATE resource
SET resource.homepage = 'https://www.southessex.ac.uk',
  resource.handle = 'south-essex-college',
  resource.index_data = 'S300 E220 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'South Essex College';

UPDATE resource
SET resource.homepage = 'https://www.stourbridge.ac.uk',
  resource.handle = 'stourbridge-college',
  resource.index_data = 'S361 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Stourbridge College';

UPDATE resource
SET resource.homepage = 'https://www.sussexdowns.ac.uk',
  resource.handle = 'sussex-downs-college',
  resource.index_data = 'S220 D520 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Sussex Downs College';

UPDATE resource
SET resource.homepage = 'https://www.swindon.ac.uk',
  resource.handle = 'swindon-college',
  resource.index_data = 'S535 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Swindon College';

UPDATE resource
SET resource.homepage = 'https://www.lcuck.ac.uk',
  resource.handle = 'london-college',
  resource.index_data = 'L535 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'London College';

UPDATE resource
SET resource.homepage = 'https://www.hud.ac.uk',
  resource.handle = 'university-of-5',
  resource.index_data = 'U516 O100 H362',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of Huddersfield';

UPDATE resource
SET resource.homepage = 'https://www.marjon.ac.uk',
  resource.handle = 'university-of-st-mark-and',
  resource.index_data = 'U516 O100 S300 M620 A530 S300 J500',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'University of St Mark and St John';

UPDATE resource
SET resource.homepage = 'https://www.warrington.ac.uk',
  resource.handle = 'warrington-collegiate',
  resource.index_data = 'W652 C423',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Warrington Collegiate';

UPDATE resource
SET resource.homepage = 'https://www.warwickshire.ac.uk',
  resource.handle = 'warwickshire-college',
  resource.index_data = 'W626 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Warwickshire College';

UPDATE resource
SET resource.homepage = 'https://www.ucy.ac.uk',
  resource.handle = 'yeovil-college',
  resource.index_data = 'Y140 C420',
  resource.quarter = 20174
WHERE resource.scope = 'UNIVERSITY'
      AND resource.name = 'Yeovil College';

INSERT INTO document(cloudinary_id, cloudinary_url, file_name, created_timestamp, updated_timestamp)
  SELECT resource.summary, resource.description, 'logo.png', '2017-11-13 12:27:31', '2017-11-13 12:27:31'
  FROM resource
  WHERE resource.scope = 'UNIVERSITY'
  AND resource.summary IS NOT NULL;

UPDATE document INNER JOIN resource
  ON document.cloudinary_id = resource.summary
SET resource.document_logo_id = document.id
WHERE resource.scope = 'UNIVERSITY';

UPDATE resource
SET resource.summary = NULL,
  resource.description = NULL
WHERE resource.scope = 'UNIVERSITY';
